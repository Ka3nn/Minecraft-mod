package com.ethem.droppermod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.Generic3x3ContainerScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.ActionResult;
import org.lwjgl.glfw.GLFW;

public class DropperModClient implements ClientModInitializer {
    public static KeyBinding keyA, keyB, keyMenu;
    private static boolean warnedConflict;

    @Override
    public void onInitializeClient() {
        DropperConfig.load();

        keyA = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.droppermod.item_a", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G, "category.droppermod"));
        keyB = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.droppermod.item_b", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_H, "category.droppermod"));
        keyMenu = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.droppermod.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_P, "category.droppermod"));

        ClientTickEvents.END_CLIENT_TICK.register(DropperModClient::tick);
        ClientEntityEvents.ENTITY_LOAD.register((entity, world) -> Illusion.onEntityLoad(entity));

        UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
            if (world.isClient) {
                BlockState st = world.getBlockState(hit.getBlockPos());
                if (st.isOf(Blocks.DROPPER) || st.isOf(Blocks.DISPENSER)) {
                    Illusion.lastUsedDropper = hit.getBlockPos().toImmutable();
                }
            }
            return ActionResult.PASS;
        });

        ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> {
            if (screen instanceof Generic3x3ContainerScreen) {
                Illusion.openDropper = Illusion.lastUsedDropper;
                ScreenEvents.remove(screen).register(s -> Illusion.openDropper = null);
            }
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> Illusion.clearAll());
    }

    private static void tick(MinecraftClient mc) {
        if (mc.player == null || mc.world == null) return;

        while (keyMenu.wasPressed()) {
            InputUtil.Key menu = KeyBindingHelper.getBoundKeyOf(keyMenu);
            InputUtil.Key social = KeyBindingHelper.getBoundKeyOf(mc.options.socialInteractionsKey);
            if (!social.equals(menu)) {
                mc.setScreen(new DropperScreen(null));
            } else if (!warnedConflict) {
                warnedConflict = true;
                Illusion.msg(mc, "Menüyü " + menu.getLocalizedText().getString()
                        + " ile açmak için Kontroller'den 'Sosyal Etkileşimler' tuşunu kaldır.");
            }
        }
        while (keyA.wasPressed()) Illusion.choose(mc, true);
        while (keyB.wasPressed()) Illusion.choose(mc, false);

        Illusion.tick(mc);
    }
}
