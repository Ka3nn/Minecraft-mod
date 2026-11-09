package com.dropperswap;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.Generic3x3ContainerScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.HashSet;
import java.util.Set;

/**
 * Bir "tur": A/B tuşuna basılır -> dropper ateşlenir -> çıkan item tespit edilir.
 * Çıkan item seçilenle aynı değilse A<->B görüntüsü takas edilir (sadece senin ekranında):
 *  - havadaki/yerdeki item, envanterdeki item ve tooltip seçtiğin item olarak görünür,
 *  - dropper'da kalan item ise diğer item olarak görünür.
 * Item dropper'a geri konup dropper ekranı kapatılınca takas kendiliğinden biter.
 */
public final class RoundState {
    private static final double RANGE_SQ = 16 * 16;
    private static final int TIMEOUT_TICKS = 20 * 60;

    private static Item choice;          // tuşla seçilen, sonucu bekleniyor
    private static Item outcome;         // gerçekte çıkan item
    private static boolean swapActive;
    private static boolean sawDropperScreen;
    private static int baseA, baseB, baseOutcome;
    private static int pendingTicks;
    private static final Set<Integer> knownEntities = new HashSet<>();

    private RoundState() { }

    public static void choose(MinecraftClient c, boolean a) {
        if (c.player == null || c.world == null || !DropperSwapConfig.valid()) return;
        Item ia = DropperSwapConfig.itemA(), ib = DropperSwapConfig.itemB();
        reset();
        choice = a ? ia : ib;
        baseA = count(c.player, ia);
        baseB = count(c.player, ib);
        for (Entity e : c.world.getEntities()) {
            if (e instanceof ItemEntity) knownEntities.add(e.getId());
        }
    }

    public static void detect(MinecraftClient c) {
        if (choice == null || c.player == null || c.world == null) return;
        if (!DropperSwapConfig.valid()) { choice = null; return; }
        Item ia = DropperSwapConfig.itemA(), ib = DropperSwapConfig.itemB();

        Item found = null;
        for (Entity e : c.world.getEntities()) {
            if (!(e instanceof ItemEntity ie) || knownEntities.contains(ie.getId())) continue;
            ItemStack s = ie.getStack();
            if (s.isEmpty()) continue; // item verisi henüz gelmedi, sonraki karede bak
            if (ie.squaredDistanceTo(c.player) <= RANGE_SQ && (s.isOf(ia) || s.isOf(ib))) {
                found = s.getItem();
                break;
            }
            knownEntities.add(ie.getId());
        }

        // Yedek yöntem: item çok hızlı alındıysa envanter sayısına bak
        if (found == null) {
            int ca = count(c.player, ia), cb = count(c.player, ib);
            if (ca > baseA && cb <= baseB) found = ia;
            else if (cb > baseB && ca <= baseA) found = ib;
        }

        if (found != null) {
            outcome = found;
            baseOutcome = found == ia ? baseA : baseB;
            swapActive = found != choice;
            sawDropperScreen = false;
            choice = null;
        }
    }

    public static void tick(MinecraftClient c) {
        if (c.player == null || c.world == null) { reset(); return; }

        if (choice != null) {
            detect(c);
            if (choice != null && ++pendingTicks > TIMEOUT_TICKS) choice = null;
        }

        if (swapActive && outcome != null) {
            if (c.currentScreen instanceof Generic3x3ContainerScreen) {
                sawDropperScreen = true;
            } else if (c.currentScreen == null && sawDropperScreen
                    && count(c.player, outcome) <= baseOutcome) {
                // Item dropper'a geri kondu ve ekran kapandı: görüntü normale döner
                swapActive = false;
                outcome = null;
                sawDropperScreen = false;
            }
        }
    }

    public static void reset() {
        choice = null;
        outcome = null;
        swapActive = false;
        sawDropperScreen = false;
        pendingTicks = 0;
        knownEntities.clear();
    }

    /** Ekranda çizilecek item: takas aktifse A<->B değiştirilir. */
    public static ItemStack display(ItemStack stack) {
        if (!swapActive || stack == null || stack.isEmpty()) return stack;
        Item ia = DropperSwapConfig.itemA(), ib = DropperSwapConfig.itemB();
        if (ia == null || ib == null) return stack;
        if (stack.isOf(ia)) return new ItemStack(ib, stack.getCount());
        if (stack.isOf(ib)) return new ItemStack(ia, stack.getCount());
        return stack;
    }

    public static String status() {
        if (choice != null) return "Seçim yapıldı, dropper bekleniyor";
        if (swapActive) return "Görüntü takası AKTİF";
        if (outcome != null) return "Çıkan item seçimle aynı (takas yok)";
        return "Hazır";
    }

    private static int count(ClientPlayerEntity p, Item item) {
        int n = 0;
        PlayerInventory inv = p.getInventory();
        for (int i = 0; i < inv.size(); i++) {
            ItemStack s = inv.getStack(i);
            if (s.isOf(item)) n += s.getCount();
        }
        ItemStack cursor = p.currentScreenHandler.getCursorStack();
        if (cursor.isOf(item)) n += cursor.getCount();
        return n;
    }
}
