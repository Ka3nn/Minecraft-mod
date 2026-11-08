package com.ethem.droppermod.mixin;

import com.ethem.droppermod.Illusion;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {
    @Shadow @Nullable protected Slot focusedSlot;
    @Shadow @Final protected ScreenHandler handler;

    /** Dropper yuvalarında itemin hangi sırada / hangi item olarak görüneceği. */
    @Redirect(method = "drawSlot", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/screen/slot/Slot;getStack()Lnet/minecraft/item/ItemStack;"))
    private ItemStack droppermod$slotStack(Slot slot) {
        return Illusion.slotDisplay(this, slot, handler);
    }

    /** Üzerine gelince çıkan isim/açıklama da görünen itemle aynı olsun. */
    @Inject(method = "drawMouseoverTooltip", at = @At("HEAD"), cancellable = true)
    private void droppermod$tooltip(DrawContext ctx, int x, int y, CallbackInfo ci) {
        if (focusedSlot == null || !focusedSlot.hasStack()) return;
        if (!handler.getCursorStack().isEmpty()) return;
        ItemStack real = focusedSlot.getStack();
        ItemStack shown = Illusion.slotDisplay(this, focusedSlot, handler);
        if (shown.isEmpty()) { ci.cancel(); return; }
        ItemStack disguise = Illusion.displayFor(shown);
        ItemStack target = disguise != null ? disguise : shown;
        if (target != real) {
            ctx.drawItemTooltip(MinecraftClient.getInstance().textRenderer, target, x, y);
            ci.cancel();
        }
    }
}
