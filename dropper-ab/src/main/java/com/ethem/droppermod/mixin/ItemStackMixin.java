package com.ethem.droppermod.mixin;

import com.ethem.droppermod.Illusion;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Hotbar'da item değiştirince çıkan isim yazısı için. Sadece görüntü. */
@Mixin(ItemStack.class)
public class ItemStackMixin {
    @Inject(method = "getName", at = @At("HEAD"), cancellable = true)
    private void droppermod$name(CallbackInfoReturnable<Text> cir) {
        ItemStack d = Illusion.displayFor((ItemStack) (Object) this);
        if (d != null) cir.setReturnValue(d.getName());
    }
}
