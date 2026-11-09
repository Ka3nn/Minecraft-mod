package com.dropperswap.mixin;

import com.dropperswap.RoundState;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Fareyle üstüne gelince çıkan tooltip (item adı) da seçilen item olsun. */
@Mixin(Screen.class)
public class ScreenMixin {
    @ModifyVariable(
            method = "getTooltipFromItem(Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/item/ItemStack;)Ljava/util/List;",
            at = @At("HEAD"), argsOnly = true, require = 0)
    private static ItemStack dropperswap$tooltip(ItemStack stack) {
        return RoundState.display(stack);
    }
}
