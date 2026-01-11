package saga.ticex_annihilation.block;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import saga.ticex_annihilation.block.entity.NeutronJammerBlockEntity;
import saga.ticex_annihilation.registries.BlockEntityRegistry;

import java.util.List;

public class NeutronJammerBlock extends BaseEntityBlock {
    public NeutronJammerBlock(Properties properties) {
        super(properties);
    }

    @Override
    @NotNull
    public RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable BlockGetter level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.add(Component.translatable("description.ticex_annihilation.marvels_of_mechanism").withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC));
        tooltip.add(Component.translatable("description.ticex_annihilation.nj_short").withStyle(ChatFormatting.RED));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new NeutronJammerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        if (!level.isClientSide && type == BlockEntityRegistry.NEUTRON_JAMMER.get()) {
            return (l, p, s, be) -> {
                if (be instanceof NeutronJammerBlockEntity jammer) {
                    NeutronJammerBlockEntity.tick(l, p, s, jammer);
                }
            };
        }
        return null;
    }
}