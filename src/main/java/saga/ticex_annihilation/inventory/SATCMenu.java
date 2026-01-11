package saga.ticex_annihilation.inventory;

import com.tacz.guns.item.ModernKineticGunItem;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;
import saga.ticex_annihilation.init.MenuInit;

import java.util.Objects;

public class SATCMenu extends AbstractContainerMenu {
    private final ItemStack containerStack;
    private final Player player;
    private static final int SLOT_COUNT = 18; // ファンネル用 (9x2)

    private final ItemStackHandler itemHandler = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            saveToNBT();
        }
    };

    public SATCMenu(int containerId, Inventory playerInv, FriendlyByteBuf buf) {
        this(containerId, playerInv, buf.readItem());
    }

    public SATCMenu(int containerId, Inventory playerInv, ItemStack stack) {
        super(MenuInit.SATC_MENU.get(), containerId);
        this.containerStack = stack;
        this.player = playerInv.player;

        if (stack.hasTag() && Objects.requireNonNull(stack.getTag()).contains("Inventory")) {
            itemHandler.deserializeNBT(stack.getTag().getCompound("Inventory"));
        }

        // --- 1. ファンネル用スロット (上部 2段) ---
        // 座標(8, 18)から開始
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 9; col++) {
                int slotIndex = col + row * 9;
                this.addSlot(new SlotItemHandler(itemHandler, slotIndex, 8 + col * 18, 18 + row * 18) {
                    @Override
                    public boolean mayPlace(@NotNull ItemStack stack) {
                        return isAllowedItem(stack);
                    }
                    @Override
                    public int getMaxStackSize() { return 1; }
                });
            }
        }

        // --- 2. プレイヤーメインインベントリ (中段 3段) ---
        // 画面外(-2000)から、見える位置(y=84)へ修正
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // --- 3. ホットバー (下段 1段) ---
        // 座標(y=142)へ修正
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 142));
        }
    }

    private boolean isAllowedItem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        // TaCZの銃かISSの魔法コンテナ、または自作MODのアイテムのみ許可
        return stack.getItem() instanceof ModernKineticGunItem ||
                ISpellContainer.isSpellContainer(stack) ||
                "ticex_annihilation".equals(stack.getItem().getCreatorModId(stack));
    }

    private void saveToNBT() {
        if (!containerStack.isEmpty()) {
            containerStack.getOrCreateTag().put("Inventory", itemHandler.serializeNBT());
            // サーバー側なら変更をクライアントへ同期
            if (!player.level().isClientSide) {
                this.broadcastChanges();
            }
        }
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (index < SLOT_COUNT) {
                // ファンネル枠 -> プレイヤー枠へ (逆方向移動)
                if (!this.moveItemStackTo(itemstack1, SLOT_COUNT, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // プレイヤー枠 -> ファンネル枠へ (許可されたアイテムのみ)
                if (isAllowedItem(itemstack1)) {
                    if (!this.moveItemStackTo(itemstack1, 0, SLOT_COUNT, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    return ItemStack.EMPTY; // 許可されていないアイテムは移動させない
                }
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
    public boolean stillValid(@NotNull Player player) {
        return !containerStack.isEmpty();
    }
}