package com.dropperswap.mixin;

import com.dropperswap.RoundState;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * 1.21.4'te envanter, hotbar, eldeki item ve yerdeki/havadaki item'ların hepsi
 * ItemModelManager.update(...) üzerinden çizilir. Tek noktadan texture değiştirilir.
 */
@Mixin(ItemModelManager.class)
public class ItemModelManagerMixin {
    @ModifyVariable(method = "update", at = @At("HEAD"), argsOnly = true)
    private ItemStack dropperswap$swap(ItemStack stack, @Local(argsOnly = true) LivingEntity entity) {
        // Başka oyuncuların elindeki itemlere dokunma
        if (entity != null && entity != MinecraftClient.getInstance().player) return stack;
        return RoundState.display(stack);
    }
}
