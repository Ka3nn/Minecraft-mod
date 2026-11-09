package com.dropperswap.mixin;

import com.dropperswap.RoundState;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Hotbar'da iteme geçince üstte çıkan isim de seçilen item olsun. */
@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Redirect(method = "renderHeldItemTooltip",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getName()Lnet/minecraft/text/Text;"),
            require = 0)
    private Text dropperswap$heldName(ItemStack stack) {
        return RoundState.display(stack).getName();
    }
}
