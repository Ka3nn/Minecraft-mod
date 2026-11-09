package com.dropperswap;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/** Oyundaki tüm itemleri ızgara halinde gösterir; aramayla süzülür, tıklayınca seçilir. */
public class ItemPickerScreen extends Screen {
    private static final int CELL = 18;
    private static final Locale TR = Locale.forLanguageTag("tr");

    private final Screen parent;
    private final Consumer<Item> onPick;
    private final List<Item> all = new ArrayList<>();
    private List<Item> filtered = new ArrayList<>();
    private TextFieldWidget search;
    private int scrollRow;
    private int gridX, gridY, cols, rows;

    public ItemPickerScreen(Screen parent, Consumer<Item> onPick) {
        super(Text.literal("Item seç"));
        this.parent = parent;
        this.onPick = onPick;
        for (Item item : Registries.ITEM) if (item != Items.AIR) all.add(item);
        filtered = new ArrayList<>(all);
    }

    @Override
    protected void init() {
        cols = Math.max(5, Math.min(18, (width - 40) / CELL));
        rows = Math.max(3, (height - 90) / CELL);
        gridX = (width - cols * CELL) / 2;
        gridY = 50;

        search = new TextFieldWidget(textRenderer, width / 2 - 100, 24, 200, 18, Text.literal("Ara"));
        search.setMaxLength(64);
        search.setPlaceholder(Text.literal("§7Ara..."));
        search.setChangedListener(this::applyFilter);
        addDrawableChild(search);
        setInitialFocus(search);

        addDrawableChild(ButtonWidget.builder(Text.literal("Geri"), b -> close())
                .dimensions(width / 2 - 50, height - 28, 100, 20).build());
    }

    private void applyFilter(String q) {
        String query = q.trim().toLowerCase(TR);
        filtered = new ArrayList<>();
        for (Item item : all) {
            if (query.isEmpty()
                    || item.getName().getString().toLowerCase(TR).contains(query)
                    || Registries.ITEM.getId(item).getPath().contains(query)) {
                filtered.add(item);
            }
        }
        scrollRow = 0;
    }

    private int maxScroll() {
        int totalRows = (filtered.size() + cols - 1) / cols;
        return Math.max(0, totalRows - rows);
    }

    private Item itemAt(double mx, double my) {
        if (mx < gridX || my < gridY || mx >= gridX + cols * CELL || my >= gridY + rows * CELL) return null;
        int c = (int) ((mx - gridX) / CELL), r = (int) ((my - gridY) / CELL);
        int idx = (scrollRow + r) * cols + c;
        return idx >= 0 && idx < filtered.size() ? filtered.get(idx) : null;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Item item = itemAt(mouseX, mouseY);
        if (item != null && button == 0) {
            onPick.accept(item);
            close();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scrollRow = MathHelper.clamp(scrollRow - (int) Math.signum(verticalAmount), 0, maxScroll());
        return true;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 10, 0xFFFFFFFF);
        context.fill(gridX - 2, gridY - 2, gridX + cols * CELL + 2, gridY + rows * CELL + 2, 0x88000000);

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int idx = (scrollRow + r) * cols + c;
                if (idx >= filtered.size()) break;
                context.drawItem(new ItemStack(filtered.get(idx)), gridX + c * CELL + 1, gridY + r * CELL + 1);
            }
        }

        Item hovered = itemAt(mouseX, mouseY);
        if (hovered != null) {
            int c = (mouseX - gridX) / CELL, r = (mouseY - gridY) / CELL;
            context.fill(gridX + c * CELL, gridY + r * CELL, gridX + c * CELL + CELL, gridY + r * CELL + CELL, 0x55FFFFFF);
            context.drawTooltip(textRenderer, hovered.getName(), mouseX, mouseY);
        }

        if (filtered.isEmpty())
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("§7Sonuç yok"), width / 2, gridY + 20, 0xFFFFFFFF);
        else if (maxScroll() > 0)
            context.drawTextWithShadow(textRenderer, Text.literal("§7Kaydır (" + (scrollRow + 1) + "/" + (maxScroll() + 1) + ")"),
                    gridX, gridY + rows * CELL + 4, 0xFFFFFFFF);
    }

    @Override
    public void close() {
        if (client != null) client.setScreen(parent);
    }
}
