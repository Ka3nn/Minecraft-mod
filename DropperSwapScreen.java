package com.dropperswap;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class DropperSwapScreen extends Screen {
    private final Screen parent;
    private String message = "";
    private int messageColor = 0xFFFFFFFF;
    private int top;

    public DropperSwapScreen(Screen parent) {
        super(Text.literal("Dropper Swap"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = width / 2;
        top = Math.max(24, height / 2 - 125);
        int y = top;

        // A satırı: [ikon + isim]  [Listeden seç] [Eldeki]
        addDrawableChild(ButtonWidget.builder(Text.literal("Listeden seç"), b -> openPicker(true))
                .dimensions(cx + 2, y + 16, 64, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Eldeki"), b -> fromHand(true))
                .dimensions(cx + 68, y + 16, 50, 20).build());
        // B satırı
        addDrawableChild(ButtonWidget.builder(Text.literal("Listeden seç"), b -> openPicker(false))
                .dimensions(cx + 2, y + 52, 64, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Eldeki"), b -> fromHand(false))
                .dimensions(cx + 68, y + 52, 50, 20).build());

        addDrawableChild(ButtonWidget.builder(autoText(), b -> {
                    DropperSwapConfig.get().autoFromDropper = !DropperSwapConfig.get().autoFromDropper;
                    DropperSwapConfig.save();
                    b.setMessage(autoText());
                }).dimensions(cx - 118, y + 84, 236, 20).build());
        addDrawableChild(ButtonWidget.builder(notifText(), b -> {
                    DropperSwapConfig.get().showNotification = !DropperSwapConfig.get().showNotification;
                    DropperSwapConfig.save();
                    b.setMessage(notifText());
                }).dimensions(cx - 118, y + 108, 236, 20).build());
        addDrawableChild(ButtonWidget.builder(persistText(), b -> {
                    DropperSwapConfig.get().persistentChoice = !DropperSwapConfig.get().persistentChoice;
                    DropperSwapConfig.save();
                    b.setMessage(persistText());
                }).dimensions(cx - 118, y + 132, 236, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Atanan itemi sıfırla"), b -> {
                    RoundState.fullReset();
                    setMessage("Atanan item sıfırlandı.", 0xFF55FF55);
                }).dimensions(cx - 118, y + 162, 117, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("A ve B'yi temizle"), b -> {
                    RoundState.fullReset();
                    DropperSwapConfig.get().itemA = "";
                    DropperSwapConfig.get().itemB = "";
                    DropperSwapConfig.save();
                    setMessage("A ve B temizlendi.", 0xFF55FF55);
                }).dimensions(cx + 1, y + 162, 117, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Kapat"), b -> close())
                .dimensions(cx - 118, y + 186, 236, 20).build());
    }

    private void openPicker(boolean a) {
        Consumer<Item> onPick = item -> assign(a, item);
        if (client != null) client.setScreen(new ItemPickerScreen(this, onPick));
    }

    private void fromHand(boolean a) {
        if (client == null || client.player == null) return;
        ItemStack held = client.player.getMainHandStack();
        if (held.isEmpty()) { setMessage("Elinde item yok.", 0xFFFF5555); return; }
        assign(a, held.getItem());
    }

    private void assign(boolean a, Item item) {
        Item other = a ? DropperSwapConfig.itemB() : DropperSwapConfig.itemA();
        if (item == other) { setMessage("A ve B farklı itemler olmalı.", 0xFFFF5555); return; }
        if (a) DropperSwapConfig.setA(item); else DropperSwapConfig.setB(item);
        RoundState.fullReset();
        setMessage((a ? "A" : "B") + " atandı: " + item.getName().getString(), 0xFF55FF55);
    }

    private static Text onOff(String label, boolean on, String onWord, String offWord) {
        return Text.literal(label + ": " + (on ? "§a" + onWord : "§c" + offWord));
    }
    private static Text autoText() {
        return onOff("Dropper'dan otomatik A/B al", DropperSwapConfig.get().autoFromDropper, "Açık", "Kapalı");
    }
    private static Text notifText() {
        return onOff("Bildirim yazısı", DropperSwapConfig.get().showNotification, "Açık", "Gizli");
    }
    private static Text persistText() {
        return onOff("Seçim her lever'da geçerli", DropperSwapConfig.get().persistentChoice, "Açık", "Kapalı");
    }

    private void setMessage(String m, int color) { message = m; messageColor = color; }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        int cx = width / 2;
        context.drawCenteredTextWithShadow(textRenderer, title, cx, top - 14, 0xFFFFFFFF);
        drawSlot(context, "A itemi", DropperSwapConfig.itemA(), cx - 118, top + 4);
        drawSlot(context, "B itemi", DropperSwapConfig.itemB(), cx - 118, top + 40);

        Item choice = RoundState.currentChoice();
        String atanan = choice == null ? "yok" : choice.getName().getString();
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Atanan: " + atanan + "  |  " + RoundState.status()),
                cx, top + 212, 0xFFAAAAAA);
        if (!message.isEmpty())
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(message), cx, top + 226, messageColor);
    }

    private void drawSlot(DrawContext context, String label, Item item, int x, int y) {
        context.drawTextWithShadow(textRenderer, Text.literal(label), x, y, 0xFFFFFFFF);
        context.fill(x, y + 12, x + 18, y + 30, 0xFF8B8B8B);
        context.fill(x + 1, y + 13, x + 17, y + 29, 0xFF373737);
        if (item != null) {
            context.drawItem(new ItemStack(item), x + 1, y + 13);
            context.drawTextWithShadow(textRenderer, item.getName(), x + 22, y + 17, 0xFFFFFFFF);
        } else {
            context.drawTextWithShadow(textRenderer, Text.literal("§7(boş)"), x + 22, y + 17, 0xFFFFFFFF);
        }
    }

    @Override
    public void close() {
        if (client != null) client.setScreen(parent);
    }
}
