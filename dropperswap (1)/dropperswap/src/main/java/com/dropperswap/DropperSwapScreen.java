package com.dropperswap;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;

public class DropperSwapScreen extends Screen {
    private final Screen parent;
    private TextFieldWidget fieldA, fieldB;
    private String message = "";
    private int messageColor = 0xFFFFFFFF;

    public DropperSwapScreen(Screen parent) {
        super(Text.literal("Dropper Swap"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = width / 2;
        int y = Math.max(30, height / 2 - 120);

        fieldA = new TextFieldWidget(textRenderer, cx - 100, y + 20, 150, 20, Text.literal("A"));
        fieldA.setMaxLength(128);
        fieldA.setText(DropperSwapConfig.get().itemA);
        addDrawableChild(fieldA);
        addDrawableChild(ButtonWidget.builder(Text.literal("Eldeki"), b -> fromHand(fieldA))
                .dimensions(cx + 54, y + 20, 46, 20).build());

        fieldB = new TextFieldWidget(textRenderer, cx - 100, y + 60, 150, 20, Text.literal("B"));
        fieldB.setMaxLength(128);
        fieldB.setText(DropperSwapConfig.get().itemB);
        addDrawableChild(fieldB);
        addDrawableChild(ButtonWidget.builder(Text.literal("Eldeki"), b -> fromHand(fieldB))
                .dimensions(cx + 54, y + 60, 46, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Kaydet"), b -> save())
                .dimensions(cx - 100, y + 96, 98, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Kapat"), b -> close())
                .dimensions(cx + 2, y + 96, 98, 20).build());
        addDrawableChild(ButtonWidget.builder(notifText(), b -> {
                    DropperSwapConfig.get().showNotification = !DropperSwapConfig.get().showNotification;
                    DropperSwapConfig.save();
                    b.setMessage(notifText());
                }).dimensions(cx - 100, y + 144, 200, 20).build());
        addDrawableChild(ButtonWidget.builder(persistText(), b -> {
                    DropperSwapConfig.get().persistentChoice = !DropperSwapConfig.get().persistentChoice;
                    DropperSwapConfig.save();
                    b.setMessage(persistText());
                }).dimensions(cx - 100, y + 168, 200, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Görüntüyü sıfırla"), b -> {
                    RoundState.fullReset();
                    setMessage("Görüntü normale döndü.", 0xFF55FF55);
                }).dimensions(cx - 100, y + 120, 98, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Her şeyi sıfırla"), b -> {
                    RoundState.fullReset();
                    DropperSwapConfig.get().itemA = "";
                    DropperSwapConfig.get().itemB = "";
                    DropperSwapConfig.save();
                    fieldA.setText("");
                    fieldB.setText("");
                    setMessage("A, B ve görüntü sıfırlandı.", 0xFF55FF55);
                }).dimensions(cx + 2, y + 120, 98, 20).build());
    }

    private void fromHand(TextFieldWidget field) {
        if (client == null || client.player == null) return;
        ItemStack held = client.player.getMainHandStack();
        if (held.isEmpty()) { setMessage("Elinde item yok.", 0xFFFF5555); return; }
        field.setText(Registries.ITEM.getId(held.getItem()).toString());
    }

    private void save() {
        Item a = DropperSwapConfig.parse(fieldA.getText());
        Item b = DropperSwapConfig.parse(fieldB.getText());
        if (a == null || b == null) { setMessage("Geçersiz item ID (örn. minecraft:diamond)", 0xFFFF5555); return; }
        if (a == b) { setMessage("A ve B farklı itemler olmalı.", 0xFFFF5555); return; }
        DropperSwapConfig.get().itemA = Registries.ITEM.getId(a).toString();
        DropperSwapConfig.get().itemB = Registries.ITEM.getId(b).toString();
        DropperSwapConfig.save();
        RoundState.fullReset();
        setMessage("Kaydedildi. Şimdi A veya B tuşuna bas.", 0xFF55FF55);
    }

    private static Text notifText() {
        return Text.literal("Bildirim yazısı: " + (DropperSwapConfig.get().showNotification ? "§aAçık" : "§cGizli"));
    }

    private static Text persistText() {
        return Text.literal("Seçim kalıcı (her lever): " + (DropperSwapConfig.get().persistentChoice ? "§aAçık" : "§cKapalı"));
    }

    private void setMessage(String m, int color) { message = m; messageColor = color; }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        int cx = width / 2;
        int y = Math.max(30, height / 2 - 120);
        context.drawCenteredTextWithShadow(textRenderer, title, cx, y - 20, 0xFFFFFFFF);
        context.drawTextWithShadow(textRenderer, Text.literal("A itemi:"), cx - 100, y + 8, 0xFFFFFFFF);
        context.drawTextWithShadow(textRenderer, Text.literal("B itemi:"), cx - 100, y + 48, 0xFFFFFFFF);

        Item a = DropperSwapConfig.parse(fieldA.getText());
        Item b = DropperSwapConfig.parse(fieldB.getText());
        if (a != null) context.drawItem(new ItemStack(a), cx - 122, y + 22);
        if (b != null) context.drawItem(new ItemStack(b), cx - 122, y + 62);

        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Durum: " + RoundState.status()), cx, y + 196, 0xFFAAAAAA);
        if (!message.isEmpty())
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(message), cx, y + 210, messageColor);
    }

    @Override
    public void close() {
        if (client != null) client.setScreen(parent);
    }
}
