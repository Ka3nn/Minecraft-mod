package com.ethem.droppermod;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class DropperScreen extends Screen {
    private final Screen parent;
    private TextFieldWidget fieldA, fieldB;
    private ButtonWidget keyABtn, keyBBtn, keyMenuBtn, autoBtn;
    private KeyBinding capturing;
    private String status = "";

    public DropperScreen(Screen parent) {
        super(Text.literal("Dropper A/B"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        DropperConfig cfg = DropperConfig.INSTANCE;
        int cx = width / 2;
        int y = 44;

        fieldA = new TextFieldWidget(textRenderer, cx - 120, y, 150, 20, Text.literal("Item A"));
        fieldA.setMaxLength(128);
        fieldA.setText(cfg.itemA);
        addDrawableChild(fieldA);
        addDrawableChild(ButtonWidget.builder(Text.literal("Elimdeki → A"), b -> fromHand(true))
                .dimensions(cx + 36, y, 100, 20).build());

        y += 26;
        fieldB = new TextFieldWidget(textRenderer, cx - 120, y, 150, 20, Text.literal("Item B"));
        fieldB.setMaxLength(128);
        fieldB.setText(cfg.itemB);
        addDrawableChild(fieldB);
        addDrawableChild(ButtonWidget.builder(Text.literal("Elimdeki → B"), b -> fromHand(false))
                .dimensions(cx + 36, y, 100, 20).build());

        y += 26;
        addDrawableChild(ButtonWidget.builder(Text.literal("Son açtığım dropper'dan A/B ata"), b -> fromDropper())
                .dimensions(cx - 150, y, 300, 20).build());

        y += 32;
        keyABtn = addDrawableChild(ButtonWidget.builder(Text.empty(), b -> startCapture(DropperModClient.keyA))
                .dimensions(cx - 150, y, 146, 20).build());
        keyBBtn = addDrawableChild(ButtonWidget.builder(Text.empty(), b -> startCapture(DropperModClient.keyB))
                .dimensions(cx + 4, y, 146, 20).build());

        y += 24;
        keyMenuBtn = addDrawableChild(ButtonWidget.builder(Text.empty(), b -> startCapture(DropperModClient.keyMenu))
                .dimensions(cx - 150, y, 146, 20).build());
        autoBtn = addDrawableChild(ButtonWidget.builder(Text.empty(), b -> {
                    cfg.autoPressButton = !cfg.autoPressButton;
                    refreshLabels();
                }).dimensions(cx + 4, y, 146, 20).build());

        y += 32;
        addDrawableChild(ButtonWidget.builder(Text.literal("İllüzyonu sıfırla"), b -> {
                    Illusion.reset();
                    status = "Sıfırlandı.";
                }).dimensions(cx - 150, y, 146, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Kaydet ve kapat"), b -> close())
                .dimensions(cx + 4, y, 146, 20).build());

        refreshLabels();
    }

    private void fromHand(boolean a) {
        if (client == null || client.player == null) return;
        ItemStack hand = client.player.getMainHandStack();
        if (hand.isEmpty()) { status = "Elin boş."; return; }
        String id = DropperConfig.idOf(hand.getItem());
        (a ? fieldA : fieldB).setText(id);
        status = (a ? "A" : "B") + " = " + id;
    }

    private void fromDropper() {
        ItemStack[] c = Illusion.lastSeenContents;
        if (c == null) { status = "Önce bir dropper'ı aç."; return; }
        Item first = null, second = null;
        for (ItemStack s : c) {
            if (s == null || s.isEmpty()) continue;
            if (first == null) first = s.getItem();
            else if (s.getItem() != first) { second = s.getItem(); break; }
        }
        if (first == null || second == null) { status = "Dropper'da 2 farklı item yok."; return; }
        fieldA.setText(DropperConfig.idOf(first));
        fieldB.setText(DropperConfig.idOf(second));
        status = "A ve B dropper'daki sıraya göre atandı.";
    }

    private void startCapture(KeyBinding kb) {
        capturing = kb;
        refreshLabels();
    }

    private void refreshLabels() {
        keyABtn.setMessage(keyLabel("A tuşu", DropperModClient.keyA));
        keyBBtn.setMessage(keyLabel("B tuşu", DropperModClient.keyB));
        keyMenuBtn.setMessage(keyLabel("Menü", DropperModClient.keyMenu));
        autoBtn.setMessage(Text.literal("Butona bas: " + (DropperConfig.INSTANCE.autoPressButton ? "AÇIK" : "KAPALI")));
    }

    private Text keyLabel(String name, KeyBinding kb) {
        if (capturing == kb) return Text.literal(name + ": > tuşa bas <");
        return Text.literal(name + ": ").append(kb.getBoundKeyLocalizedText());
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (capturing != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) capturing.setBoundKey(InputUtil.UNKNOWN_KEY);
            else capturing.setBoundKey(InputUtil.fromKeyCode(keyCode, scanCode));
            KeyBinding.updateKeysByCode();
            if (client != null) client.options.write();
            capturing = null;
            refreshLabels();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        int cx = width / 2;
        ctx.drawCenteredTextWithShadow(textRenderer, title, cx, 16, 0xFFFFFF);
        ctx.drawTextWithShadow(textRenderer, "A:", cx - 150, 50, 0xFFFFFF);
        ctx.drawTextWithShadow(textRenderer, "B:", cx - 150, 76, 0xFFFFFF);
        drawIcon(ctx, fieldA.getText(), cx - 140, 46);
        drawIcon(ctx, fieldB.getText(), cx - 140, 72);
        if (!status.isEmpty()) ctx.drawCenteredTextWithShadow(textRenderer, Text.literal(status), cx, height - 30, 0xFFFF55);
        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Örnek: diamond, minecraft:emerald · Esc ile tuşu kaldırırsın"), cx, height - 16, 0xAAAAAA);
    }

    private void drawIcon(DrawContext ctx, String id, int x, int y) {
        Item item = DropperConfig.parseItem(id);
        if (item != null) ctx.drawItem(new ItemStack(item), x, y);
        else if (!id.isBlank()) ctx.drawTextWithShadow(textRenderer, "?", x + 5, y + 4, 0xFF5555);
    }

    @Override
    public void close() {
        DropperConfig cfg = DropperConfig.INSTANCE;
        cfg.itemA = fieldA.getText().trim();
        cfg.itemB = fieldB.getText().trim();
        DropperConfig.save();
        if (client != null) client.setScreen(parent);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
