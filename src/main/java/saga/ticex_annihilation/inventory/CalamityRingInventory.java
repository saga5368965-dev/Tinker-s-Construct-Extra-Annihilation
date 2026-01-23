package saga.ticex_annihilation.inventory;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

public class CalamityRingInventory extends ItemStackHandler {
    // 1ページ54スロット(9x6)として、最大1000スロット程度を確保（実質無限）
    public static final int MAX_SLOTS = 1000;
    private final ItemStack ringStack;

    public CalamityRingInventory(ItemStack stack) {
        super(MAX_SLOTS);
        this.ringStack = stack;

        // 保存されているNBTがあれば読み込む
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("CalamityInventory")) {
            this.deserializeNBT(tag.getCompound("CalamityInventory"));
        }
    }

    @Override
    protected void onContentsChanged(int slot) {
        // アイテムが変更されたら即座に指輪本体のNBTを更新
        CompoundTag tag = ringStack.getOrCreateTag();
        tag.put("CalamityInventory", this.serializeNBT());
    }

    // 特定のページのスロット範囲を扱う際に使用する予定
    public int getPageOffset(int page) {
        return page * 54;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64; // 重なるアイテムは通常通り64個まで
    }
}
