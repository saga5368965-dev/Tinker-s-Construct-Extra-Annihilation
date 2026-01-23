package saga.ticex_annihilation.inventory;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler; // IItemHandler用のスロット
import saga.ticex_annihilation.registries.MenuRegistry;

public class CalamityArmyMenu extends AbstractContainerMenu {
    private final ItemStack ringStack;

    public CalamityArmyMenu(int id, Inventory playerInv, ItemStack ringStack) {
        // MenuRegistry.CALAMITY_ARMY_MENU が正しく登録されていることを確認
        super(MenuRegistry.CALAMITY_ARMY_MENU.get(), id);
        this.ringStack = ringStack;

        // 指輪専用のインベントリハンドラ (IItemHandlerを実装している前提)
        CalamityRingInventory ringInv = new CalamityRingInventory(ringStack);

        int startX = 180;
        int startY = 20;

        // 支配専用スロット (200-204)
        // CalamityRingInventoryがIItemHandlerを実装している場合、SlotItemHandlerを使用します
        for (int i = 0; i < 5; i++) {
            this.addSlot(new SlotItemHandler(ringInv, 200 + i, startX, startY + (i * 20)));
        }

        // 3. プレイヤーインベントリ
        layoutPlayerInventorySlots(playerInv, 48, 130);
    }

    private void layoutPlayerInventorySlots(Inventory inventory, int x, int y) {
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(inventory, j + i * 9 + 9, x + j * 18, y + i * 18));
            }
        }
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(inventory, i, x + i * 18, y + 58));
        }
    }

    public ItemStack getRingStack() { return ringStack; }

    @Override
    public boolean stillValid(Player player) {
        return !this.ringStack.isEmpty();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        // シフトクリック時の挙動（とりあえず空返しでエラー回避、必要なら後で実装）
        return ItemStack.EMPTY;
    }
}