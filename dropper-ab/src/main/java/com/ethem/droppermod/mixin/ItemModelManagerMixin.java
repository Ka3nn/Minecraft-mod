package com.ethem.droppermod.mixin;

import com.ethem.droppermod.Illusion;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** 1.21.4'te GUI, el, yerdeki item, oyuncunun elindeki item... hepsi buradan geçer. */
@Mixin(ItemModelManager.class)
public class ItemModelManagerMixin {
    @ModifyVariable(method = "update", at = @At("HEAD"), argsOnly = true)
    private ItemStack droppermod$swap(ItemStack stack) {
        ItemStack d = Illusion.displayFor(stack);
        return d != null ? d : stack;
    }
}
