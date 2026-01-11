package saga.ticex_annihilation.modifiable;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import slimeknights.tconstruct.library.tools.definition.ToolDefinition;
import slimeknights.tconstruct.library.tools.item.ModifiableItem;
import slimeknights.tconstruct.library.utils.Util;
import moffy.ticex.client.modules.ticex.UnsyncedToolContainerMenu;

import java.util.List;

public class S_A_T_C extends ModifiableItem {

    public S_A_T_C(Item.Properties properties, ToolDefinition toolDefinition) {
        super(properties, toolDefinition);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.isCrouching()) {
            // スニーク時はインベントリを開く
            InteractionResult result = UnsyncedToolContainerMenu.tryOpenContainer(stack, null, getToolDefinition(), player, Util.getSlotType(hand));
            if (result.consumesAction()) {
                return new InteractionResultHolder<>(result, stack);
            }
        } else {
            // 通常右クリックでファンネルの起動・停止を切り替え
            if (level.isClientSide) {
                player.displayClientMessage(Component.translatable("message.ticex_annihilation.toggle"), true);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        return super.use(level, player, hand);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        // 内部スロットに何が入っているかツールチップに表示
        stack.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(itemHandler -> {
            boolean hasItems = false;
            for (int i = 0; i < itemHandler.getSlots(); i++) {
                ItemStack stackInSlot = itemHandler.getStackInSlot(i);
                if (!stackInSlot.isEmpty()) {
                    if (!hasItems) {
                        tooltip.add(Component.translatable("tooltip.ticex_annihilation.equipped_units").withStyle(net.minecraft.ChatFormatting.GOLD));
                        hasItems = true;
                    }
                    tooltip.add(Component.literal(" - ").append(stackInSlot.getDisplayName()).withStyle(net.minecraft.ChatFormatting.GRAY));
                }
            }
        });
    }
}
