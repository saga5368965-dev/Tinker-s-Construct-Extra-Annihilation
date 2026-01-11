package saga.ticex_annihilation.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import saga.ticex_annihilation.inventory.SATCMenu;

public class SATCScreen extends AbstractContainerScreen<SATCMenu> {

    public SATCScreen(SATCMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // 背景：深い緑のグラデーション
        graphics.fillGradient(x, y, x + imageWidth, y + imageHeight, 0xFF0A1F0A, 0xFF051005);
        // 外枠
        graphics.renderOutline(x, y, imageWidth, imageHeight, 0xFF1B4D1B);

        // 全スロットの枠組みをダークグリーンで描画
        for (int i = 0; i < this.menu.slots.size(); i++) {
            var slot = this.menu.slots.get(i);
            int sx = x + slot.x - 1;
            int sy = y + slot.y - 1;

            // スロットの縁取り（ダークグリーン）
            graphics.renderOutline(sx, sy, 18, 18, 0xFF1B4D1B);
            // スロット内部をさらに暗くしてアイテムを際立たせる
            graphics.fill(sx + 1, sy + 1, sx + 17, sy + 17, 0xFF020802);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}