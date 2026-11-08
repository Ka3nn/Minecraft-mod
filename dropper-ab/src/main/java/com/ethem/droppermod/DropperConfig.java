package com.ethem.droppermod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/** config/droppermod.json içinde saklanan ayarlar. Tuşlar options.txt'de saklanır. */
public class DropperConfig {
    public String itemA = "";
    public String itemB = "";
    /** A/B tuşuna basınca baktığın butona / levere otomatik basılsın mı */
    public boolean autoPressButton = true;

    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("droppermod.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static DropperConfig INSTANCE = new DropperConfig();

    private transient String cachedAKey, cachedBKey;
    private transient Item cachedA, cachedB;

    public static void load() {
        try {
            if (Files.exists(PATH)) {
                DropperConfig c = GSON.fromJson(Files.readString(PATH), DropperConfig.class);
                if (c != null) INSTANCE = c;
            }
        } catch (Exception e) {
            System.err.println("[droppermod] config okunamadi: " + e);
        }
    }

    public static void save() {
        try {
            Files.createDirectories(PATH.getParent());
            Files.writeString(PATH, GSON.toJson(INSTANCE));
        } catch (Exception e) {
            System.err.println("[droppermod] config yazilamadi: " + e);
        }
    }

    public Item getA() {
        if (!itemA.equals(cachedAKey)) { cachedAKey = itemA; cachedA = parseItem(itemA); }
        return cachedA;
    }

    public Item getB() {
        if (!itemB.equals(cachedBKey)) { cachedBKey = itemB; cachedB = parseItem(itemB); }
        return cachedB;
    }

    public static Item parseItem(String s) {
        if (s == null || s.isBlank()) return null;
        String t = s.trim().toLowerCase(Locale.ROOT);
        if (!t.contains(":")) t = "minecraft:" + t;
        Identifier id = Identifier.tryParse(t);
        if (id == null) return null;
        for (Item item : Registries.ITEM) {
            if (Registries.ITEM.getId(item).equals(id)) return item == Items.AIR ? null : item;
        }
        return null;
    }

    public static String idOf(Item item) {
        return Registries.ITEM.getId(item).toString();
    }
}
