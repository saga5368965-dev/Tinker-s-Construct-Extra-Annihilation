package saga.ticex_annihilation.inventory;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import saga.ticex_annihilation.item.CalamityRingItem;
import saga.ticex_annihilation.registries.MenuRegistry;

public class CalamityRingMenu extends AbstractContainerMenu {
    private final ItemStack ringStack;
    private final CalamityRingInventory ringInventory;

    // クライアント側（FriendlyByteBufを使う方）
    public CalamityRingMenu(int id, Inventory playerInv, FriendlyByteBuf buf) {
        this(id, playerInv, buf.readItem());
    }

    // サーバー・クライアント共通のメインコンストラクタ
    public CalamityRingMenu(int id, Inventory playerInv, ItemStack ringStack) {
        super(MenuRegistry.CALAMITY_RING_MENU.get(), id);
        this.ringStack = ringStack;
        this.ringInventory = new CalamityRingInventory(ringStack);

        // 1. 指輪の内部スロット (9x6 = 54スロット) を追加
        // 座標(8, 18)から開始するチェスト標準のレイアウト
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new SlotItemHandler(ringInventory, col + row * 9, 8 + col * 18, 18 + row * 18));
            }
        }

        // 2. プレイヤーのインベントリ (下部の持ち物枠) を追加
        int playerInvY = 140; // チェストUIの高さに合わせて調整
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, playerInvY + row * 18));
            }
        }

        // 3. ホットバー (最下部の9枠) を追加
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, playerInvY + 58));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        // 指輪を手から離したり捨てたりしたら閉じる
        return !this.ringStack.isEmpty() && player.getMainHandItem() == this.ringStack;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index < 54) { // 指輪からプレイヤーへ
                if (!this.moveItemStackTo(itemstack1, 54, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, 54, false)) { // プレイヤーから指輪へ
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemstack;
    }
    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide && !this.ringStack.isEmpty()) {
            if (this.ringStack.getItem() instanceof CalamityRingItem ringItem) {
                // インベントリを閉じた時に1000スロットを再スキャンしてキャッシュを更新
                ringItem.updateAttributeCache(this.ringStack);
            }
        }
    }
}
