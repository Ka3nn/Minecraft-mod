package com.dropperswap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.nio.file.Files;
import java.nio.file.Path;

public class DropperSwapConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static DropperSwapConfig INSTANCE = new DropperSwapConfig();

    public String itemA = "";
    public String itemB = "";
    /** A/B tuşuna basınca ekranda "A seçildi" yazısı çıksın mı */
    public boolean showNotification = true;
    /** Seçim her lever çevirişinde geçerli kalsın (tekrar tuşa basmak gerekmez) */
    public boolean persistentChoice = true;
    /** Dropper açılınca içindeki 2 farklı item otomatik A ve B olsun */
    public boolean autoFromDropper = true;

    private transient boolean resolved;
    private transient Item cachedA, cachedB;

    public static DropperSwapConfig get() { return INSTANCE; }

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("dropperswap.json");
    }

    public static void load() {
        try {
            Path p = path();
            if (Files.exists(p)) {
                DropperSwapConfig c = GSON.fromJson(Files.readString(p), DropperSwapConfig.class);
                if (c != null) INSTANCE = c;
            }
        } catch (Exception e) {
            INSTANCE = new DropperSwapConfig();
        }
        if (INSTANCE.itemA == null) INSTANCE.itemA = "";
        if (INSTANCE.itemB == null) INSTANCE.itemB = "";
        INSTANCE.resolved = false;
    }

    public static void save() {
        try {
            Files.writeString(path(), GSON.toJson(INSTANCE));
        } catch (Exception ignored) { }
        INSTANCE.resolved = false;
    }

    public static Item parse(String s) {
        if (s == null || s.isBlank()) return null;
        Identifier id = Identifier.tryParse(s.trim().toLowerCase());
        if (id == null || !Registries.ITEM.containsId(id)) return null;
        Item item = Registries.ITEM.get(id);
        return item == Items.AIR ? null : item;
    }

    private void resolve() {
        if (resolved) return;
        cachedA = parse(itemA);
        cachedB = parse(itemB);
        resolved = true;
    }

    public static void setA(Item item) { INSTANCE.itemA = Registries.ITEM.getId(item).toString(); save(); }
    public static void setB(Item item) { INSTANCE.itemB = Registries.ITEM.getId(item).toString(); save(); }

    public static Item itemA() { INSTANCE.resolve(); return INSTANCE.cachedA; }
    public static Item itemB() { INSTANCE.resolve(); return INSTANCE.cachedB; }

    /** A ve B ayarlı ve birbirinden farklı mı? */
    public static boolean valid() {
        Item a = itemA(), b = itemB();
        return a != null && b != null && a != b;
    }
}
