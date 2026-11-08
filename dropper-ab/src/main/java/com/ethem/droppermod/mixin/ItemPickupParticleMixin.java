package com.ethem.droppermod.mixin;

import com.ethem.droppermod.Illusion;
import net.minecraft.client.particle.ItemPickupParticle;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Item envantere uçarken (toplanma animasyonu) de seçilen item görünsün. */
@Mixin(ItemPickupParticle.class)
public class ItemPickupParticleMixin {
    @Inject(method = "getOrCopy", at = @At("RETURN"), require = 0)
    private void droppermod$copy(Entity entity, CallbackInfoReturnable<Entity> cir) {
        Illusion.onPickupCopy(entity, cir.getReturnValue());
    }
}
