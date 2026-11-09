package com.dropperswap;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class DropperSwapClient implements ClientModInitializer {
    public static final String CATEGORY = "key.categories.dropperswap";
    public static KeyBinding keyA, keyB, keyMenu, keyReset;

    @Override
    public void onInitializeClient() {
        DropperSwapConfig.load();

        keyA = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.dropperswap.a", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_KP_1, CATEGORY));
        keyB = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.dropperswap.b", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_KP_2, CATEGORY));
        // P = vanilla "Sosyal Etkileşimler" tuşu. O tuş kaldırılınca P bu menüyü açar.
        keyMenu = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.dropperswap.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_P, CATEGORY));
        keyReset = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.dropperswap.reset", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, CATEGORY));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (keyMenu.wasPressed()) client.setScreen(new DropperSwapScreen(client.currentScreen));
            while (keyA.wasPressed()) RoundState.choose(client, true);
            while (keyB.wasPressed()) RoundState.choose(client, false);
            while (keyReset.wasPressed()) RoundState.reset();
            RoundState.tick(client);
        });

        // Her karede de kontrol et: item dropper'dan çıktığı ilk karede bile doğru görünsün.
        WorldRenderEvents.START.register(ctx -> RoundState.detect(MinecraftClient.getInstance()));
    }
}
