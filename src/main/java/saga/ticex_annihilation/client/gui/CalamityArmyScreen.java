package saga.ticex_annihilation.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.sounds.SoundEvents;
import saga.ticex_annihilation.client.ClientEntityCache;
import saga.ticex_annihilation.inventory.CalamityArmyMenu;
import saga.ticex_annihilation.registries.network.PacketHandler;
import saga.ticex_annihilation.registries.network.SelectServantPacket;

import java.util.ArrayList;
import java.util.List;

public class CalamityArmyScreen extends AbstractContainerScreen<CalamityArmyMenu> {
    private int selectedIndex = 0;
    private final List<String> armyIds = new ArrayList<>();

    public CalamityArmyScreen(CalamityArmyMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 256;
        this.imageHeight = 220;
        loadArmyFromNBT();
    }

    private void loadArmyFromNBT() {
        CompoundTag nbt = menu.getRingStack().getOrCreateTag();
        ListTag list = nbt.getList("ServantArmy", Tag.TAG_COMPOUND);
        armyIds.clear();
        for (int i = 0; i < list.size(); i++) {
            armyIds.add(list.getCompound(i).getString("id"));
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.fillGradient(0, 0, this.width, this.height, 0xAA000000, 0xEE000000);

        if (armyIds.isEmpty()) {
            guiGraphics.drawCenteredString(this.font, Component.translatable("gui.ticex_annihilation.army.empty"), this.width / 2, this.height / 2, 0xFFFFFF);
            return;
        }

        // --- 左側：縦型3Dプレビュー ---
        int centerX = 60;
        int centerY = this.height / 2 - 20;

        for (int i = -2; i <= 2; i++) {
            int index = selectedIndex + i;
            if (index < 0 || index >= armyIds.size()) continue;

            LivingEntity entity = ClientEntityCache.getOrCreate(armyIds.get(index));
            if (entity != null) {
                // 右側の専用スロットに入っている装備を、プレビュー用エンティティに同期
                if (i == 0) {
                    syncPreviewEquipment(entity);
                }

                int entityY = centerY + (i * 50);
                float scale = (i == 0) ? 45f : 20f;
                InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, centerX, entityY + 30, (int)scale, centerX - mouseX, entityY - mouseY, entity);

                if (i == 0) {
                    guiGraphics.drawString(this.font, Component.translatable("gui.ticex_annihilation.army.count", getMobCount(index)), centerX + 40, entityY, 0xFFFFFF);
                }
            }
        }

        // --- 右側：スロットラベル ---
        int slotX = 180 + (width - imageWidth) / 2;
        guiGraphics.drawString(this.font, Component.translatable("gui.ticex_annihilation.army.equip_title"), width / 2 + 50, 10, 0xFFFFFF);
    }

    // プレビューのモブに装備を着せる
    private void syncPreviewEquipment(LivingEntity entity) {
        entity.setItemSlot(EquipmentSlot.MAINHAND, menu.getSlot(0).getItem()); // 200番スロット
        entity.setItemSlot(EquipmentSlot.HEAD, menu.getSlot(1).getItem());     // 201番
        entity.setItemSlot(EquipmentSlot.CHEST, menu.getSlot(2).getItem());    // 202番
        entity.setItemSlot(EquipmentSlot.LEGS, menu.getSlot(3).getItem());     // 203番
        entity.setItemSlot(EquipmentSlot.FEET, menu.getSlot(4).getItem());     // 204番
    }

    private int getMobCount(int index) {
        ListTag list = menu.getRingStack().getOrCreateTag().getList("ServantArmy", Tag.TAG_COMPOUND);
        return list.getCompound(index).getInt("count");
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // 左側のリストエリアをクリックした時のみ選択パケットを飛ばす（右側のスロット操作と競合させないため）
        if (button == 0 && mouseX < this.width / 2 - 20 && !armyIds.isEmpty()) {
            String selectedId = armyIds.get(selectedIndex);
            PacketHandler.sendToServer(new SelectServantPacket(selectedId));
            this.minecraft.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 1.0F);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (delta > 0 && selectedIndex > 0) selectedIndex--;
        else if (delta < 0 && selectedIndex < armyIds.size() - 1) selectedIndex++;
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}