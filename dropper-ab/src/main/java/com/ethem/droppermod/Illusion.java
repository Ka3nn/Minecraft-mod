package com.ethem.droppermod;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.Generic3x3ContainerScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.*;

/**
 * Tüm görsel değişikliklerin beyni. Hiçbir paket göndermez, sunucudaki hiçbir şeyi değiştirmez;
 * sadece bu bilgisayarda itemlerin NASIL ÇİZİLECEĞİNE karar verir.
 *
 * Akış:
 *  1) Dropper'ı A ve B içindeyken bir kez aç → sıralama ("layout") kaydedilir.
 *  2) A ya da B tuşuna bas → seçim yapılır (istersen baktığın butona otomatik basılır).
 *  3) Dropper'dan çıkan item entity'si yakalanır. Gerçek item seçtiğinden farklıysa
 *     yerde, toplanma animasyonunda, envanterde, elinde ve imlecinde seçtiğin item olarak çizilir.
 *  4) Dropper'ın içine bakınca seçtiğin itemin yuvası boş, diğer item kendi yuvasında görünür.
 *  5) Itemi geri koyunca dropper iki itemi de ilk sıralamasıyla gösterir, illüzyon biter.
 */
public final class Illusion {
    private Illusion() {}

    // ---- dropper bilgileri ----
    private static final Map<BlockPos, ItemStack[]> LAYOUTS = new HashMap<>();
    public static BlockPos lastUsedDropper;
    public static BlockPos openDropper;
    public static ItemStack[] lastSeenContents;

    // ---- aktif illüzyon ----
    private static boolean active;
    private static BlockPos pos;
    private static Item chosen, other, real;
    private static ItemStack chosenTemplate = ItemStack.EMPTY;
    private static long tickCounter, startTick;
    private static final List<Integer> candidates = new ArrayList<>();
    private static int trackedEntity = -1;
    private static boolean pickupPending;
    private static long pickupDeadline;
    private static int trackedSlot = -1;
    private static boolean trackedCursor;
    private static final Set<ItemStack> extraDisguised = Collections.newSetFromMap(new WeakHashMap<>());

    // ---- envanter takibi ----
    private static Item[] prevItems = new Item[0];
    private static int[] prevCounts = new int[0];
    private static Item prevCursor = Items.AIR;
    private static int lastGainSlot = -1;
    private static long lastGainTick = -1000;
    private static long lastUpdateNanos;

    // =====================================================================
    // Olaylar
    // =====================================================================

    public static void choose(MinecraftClient mc, boolean isA) {
        DropperConfig cfg = DropperConfig.INSTANCE;
        Item a = cfg.getA(), b = cfg.getB();
        if (a == null || b == null) { msg(mc, "Önce menüden (P) A ve B itemlerini ayarla."); return; }
        if (a == b) { msg(mc, "A ve B farklı itemler olmalı."); return; }
        if (active && (trackedSlot >= 0 || trackedCursor || trackedEntity >= 0 || pickupPending)) {
            msg(mc, "Önce önceki itemi dropper'a geri koy (ya da menüden sıfırla).");
            return;
        }
        reset();
        active = true;
        chosen = isA ? a : b;
        other = isA ? b : a;
        pos = findDropper(mc);
        chosenTemplate = templateFor(pos, chosen);
        startTick = tickCounter;
        snapshot(mc.player.getInventory(), mc.player.currentScreenHandler.getCursorStack());
        if (cfg.autoPressButton) pressButton(mc);
    }

    public static void onEntityLoad(Entity e) {
        if (!active || real != null || trackedEntity >= 0 || !(e instanceof ItemEntity)) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        boolean near = pos != null
                ? e.getPos().squaredDistanceTo(Vec3d.ofCenter(pos)) <= 9.0
                : e.squaredDistanceTo(mc.player) <= 64.0;
        if (near) candidates.add(e.getId());
    }

    /** Toplanma animasyonu itemin kopyasını çizer; kopyayı da kılık listesine ekle. */
    public static void onPickupCopy(Entity original, Entity copy) {
        if (!active || original == null || original.getId() != trackedEntity) return;
        if (copy instanceof ItemEntity ie && copy != original) extraDisguised.add(ie.getStack());
    }

    public static void tick(MinecraftClient mc) {
        tickCounter++;
        captureLayout(mc);
        if (!active) return;
        resolveCandidates(mc.world);
        updateTracking(mc);
    }

    public static void reset() {
        active = false;
        pos = null;
        chosen = other = real = null;
        chosenTemplate = ItemStack.EMPTY;
        candidates.clear();
        trackedEntity = -1;
        pickupPending = false;
        trackedSlot = -1;
        trackedCursor = false;
        extraDisguised.clear();
        lastGainSlot = -1;
    }

    public static void clearAll() {
        reset();
        LAYOUTS.clear();
        lastUsedDropper = null;
        openDropper = null;
        lastSeenContents = null;
    }

    // =====================================================================
    // Çizim tarafından çağrılanlar
    // =====================================================================

    /** Bu stack başka bir item gibi çizilmeli mi? null = olduğu gibi çiz. */
    public static ItemStack displayFor(ItemStack s) {
        if (!active || s == null || s.isEmpty()) return null;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (!mc.isOnThread() || mc.player == null || mc.world == null) return null;
        if (real == null) resolveCandidates(mc.world);
        if (real == null || real == chosen || !s.isOf(real)) return null;
        maybeUpdate(mc);
        if (!active) return null;
        if (isTracked(mc, s)) return chosenTemplate.copyWithCount(s.getCount());
        return null;
    }

    /** Bir GUI yuvasında hangi stack görünsün (dropper yuvaları için sıralama düzeltmesi). */
    public static ItemStack slotDisplay(Object screen, Slot slot, ScreenHandler handler) {
        ItemStack realStack = slot.getStack();
        if (!(screen instanceof Generic3x3ContainerScreen)) return realStack;
        if (slot.inventory instanceof PlayerInventory || slot.id < 0 || slot.id > 8) return realStack;
        if (openDropper == null || handler.slots.size() < 9) return realStack;
        ItemStack[] layout = LAYOUTS.get(openDropper);
        if (layout == null) return realStack;

        ItemStack[] cur = new ItemStack[9];
        for (int i = 0; i < 9; i++) cur[i] = handler.getSlot(i).getStack();
        Map<Item, Integer> realCounts = counts(cur);
        Map<Item, Integer> layoutCounts = counts(layout);

        if (active && real != null && openDropper.equals(pos)) {
            if (minusOne(layoutCounts, real).equals(realCounts)) {
                return layoutMinusOne(layout, chosen)[slot.id];
            }
        }
        if (layoutCounts.equals(realCounts)) return layout[slot.id];
        return realStack;
    }

    // =====================================================================
    // İç işler
    // =====================================================================

    private static boolean isTracked(MinecraftClient mc, ItemStack s) {
        if (extraDisguised.contains(s)) return true;
        if (trackedEntity >= 0) {
            Entity e = mc.world.getEntityById(trackedEntity);
            if (e instanceof ItemEntity ie && ie.getStack() == s) return true;
        }
        PlayerInventory inv = mc.player.getInventory();
        if (trackedSlot >= 0 && trackedSlot < inv.size() && inv.getStack(trackedSlot) == s) return true;
        return trackedCursor && mc.player.currentScreenHandler.getCursorStack() == s;
    }

    private static void resolveCandidates(ClientWorld world) {
        if (!active || real != null || candidates.isEmpty() || world == null) return;
        Iterator<Integer> it = candidates.iterator();
        while (it.hasNext()) {
            int id = it.next();
            Entity e = world.getEntityById(id);
            if (!(e instanceof ItemEntity ie) || e.isRemoved()) { it.remove(); continue; }
            ItemStack s = ie.getStack();
            if (s.isEmpty()) continue; // veri paketi henüz gelmedi
            Item item = s.getItem();
            if (item == chosen || item == other) {
                real = item;
                trackedEntity = id;
                candidates.clear();
                return;
            }
            it.remove();
        }
    }

    private static void maybeUpdate(MinecraftClient mc) {
        long now = System.nanoTime();
        if (now - lastUpdateNanos > 3_000_000L) {
            lastUpdateNanos = now;
            updateTracking(mc);
        }
    }

    private static void updateTracking(MinecraftClient mc) {
        if (!active) return;
        ClientPlayerEntity p = mc.player;
        if (p == null || mc.world == null) return;
        PlayerInventory inv = p.getInventory();
        ItemStack cursor = p.currentScreenHandler.getCursorStack();
        int n = inv.size();
        if (prevItems.length != n) { snapshot(inv, cursor); return; }

        if (real == null) {
            if (tickCounter - startTick > 100) reset(); // 5 sn içinde item çıkmadı
            snapshot(inv, cursor);
            return;
        }

        // Envantere yeni giren gerçek itemi yakala
        for (int i = 0; i < n; i++) {
            ItemStack st = inv.getStack(i);
            if (st.isOf(real) && (prevItems[i] != real || st.getCount() > prevCounts[i])) {
                lastGainSlot = i;
                lastGainTick = tickCounter;
            }
        }

        if (trackedEntity >= 0) {
            Entity e = mc.world.getEntityById(trackedEntity);
            if (e == null || e.isRemoved()) {
                trackedEntity = -1;
                pickupPending = true;
                pickupDeadline = tickCounter + 40;
            }
        }

        if (pickupPending) {
            if (lastGainSlot >= 0 && tickCounter - lastGainTick <= 20 && inv.getStack(lastGainSlot).isOf(real)) {
                trackedSlot = lastGainSlot;
                pickupPending = false;
            } else if (tickCounter > pickupDeadline) {
                reset(); // başkası aldı / despawn oldu
            }
        } else if (trackedSlot >= 0 || trackedCursor) {
            boolean still = trackedCursor ? cursor.isOf(real)
                    : (trackedSlot < n && inv.getStack(trackedSlot).isOf(real));
            if (!still) {
                int moved = -1;
                for (int i = 0; i < n; i++) {
                    if (inv.getStack(i).isOf(real) && prevItems[i] != real) { moved = i; break; }
                }
                if (moved >= 0) { trackedSlot = moved; trackedCursor = false; }
                else if (cursor.isOf(real) && prevCursor != real) { trackedCursor = true; trackedSlot = -1; }
                else reset(); // dropper'a geri konuldu ya da atıldı
            }
        }
        snapshot(inv, cursor);
    }

    private static void snapshot(PlayerInventory inv, ItemStack cursor) {
        int n = inv.size();
        if (prevItems.length != n) { prevItems = new Item[n]; prevCounts = new int[n]; }
        for (int i = 0; i < n; i++) {
            ItemStack st = inv.getStack(i);
            prevItems[i] = st.getItem();
            prevCounts[i] = st.getCount();
        }
        prevCursor = cursor.getItem();
    }

    private static void captureLayout(MinecraftClient mc) {
        if (!(mc.currentScreen instanceof Generic3x3ContainerScreen scr) || openDropper == null) return;
        ScreenHandler h = scr.getScreenHandler();
        if (h.slots.size() < 9) return;
        ItemStack[] cur = new ItemStack[9];
        for (int i = 0; i < 9; i++) cur[i] = h.getSlot(i).getStack().copy();
        lastSeenContents = cur;

        Item a = DropperConfig.INSTANCE.getA(), b = DropperConfig.INSTANCE.getB();
        if (a == null || b == null) return;
        if (active && openDropper.equals(pos)) return;
        Map<Item, Integer> c = counts(cur);
        if (c.size() == 2 && c.containsKey(a) && c.containsKey(b)) {
            ItemStack[] old = LAYOUTS.get(openDropper);
            // Aynı itemler zaten kayıtlıysa ilk sırayı koru
            if (old == null || !counts(old).equals(c)) LAYOUTS.put(openDropper.toImmutable(), cur);
        }
    }

    private static BlockPos findDropper(MinecraftClient mc) {
        Vec3d p = mc.player.getPos();
        if (lastUsedDropper != null && Vec3d.ofCenter(lastUsedDropper).squaredDistanceTo(p) <= 144) return lastUsedDropper;
        BlockPos best = null;
        double bestD = 144;
        for (BlockPos bp : LAYOUTS.keySet()) {
            double d = Vec3d.ofCenter(bp).squaredDistanceTo(p);
            if (d <= bestD) { bestD = d; best = bp; }
        }
        return best;
    }

    private static ItemStack templateFor(BlockPos at, Item item) {
        if (at != null) {
            ItemStack[] layout = LAYOUTS.get(at);
            if (layout != null) for (ItemStack s : layout) if (s.isOf(item)) return s.copyWithCount(1);
        }
        return new ItemStack(item);
    }

    private static void pressButton(MinecraftClient mc) {
        if (!(mc.crosshairTarget instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) return;
        if (mc.interactionManager == null || mc.world == null) return;
        BlockState st = mc.world.getBlockState(hit.getBlockPos());
        if (st.isIn(BlockTags.BUTTONS) || st.isOf(Blocks.LEVER)) {
            ActionResult r = mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
            if (r.isAccepted()) mc.player.swingHand(Hand.MAIN_HAND);
        }
    }

    private static Map<Item, Integer> counts(ItemStack[] arr) {
        Map<Item, Integer> m = new HashMap<>();
        for (ItemStack s : arr) if (s != null && !s.isEmpty()) m.merge(s.getItem(), s.getCount(), Integer::sum);
        return m;
    }

    private static Map<Item, Integer> minusOne(Map<Item, Integer> m, Item item) {
        Map<Item, Integer> r = new HashMap<>(m);
        Integer c = r.get(item);
        if (c == null) return r;
        if (c <= 1) r.remove(item); else r.put(item, c - 1);
        return r;
    }

    private static ItemStack[] layoutMinusOne(ItemStack[] layout, Item item) {
        ItemStack[] r = layout.clone();
        for (int i = 0; i < r.length; i++) {
            if (r[i].isOf(item)) {
                r[i] = r[i].getCount() > 1 ? r[i].copyWithCount(r[i].getCount() - 1) : ItemStack.EMPTY;
                break;
            }
        }
        return r;
    }

    static void msg(MinecraftClient mc, String s) {
        if (mc.player != null) mc.player.sendMessage(Text.literal("§6[Dropper]§r " + s), false);
    }

    public static boolean isActive() { return active; }
}
