package saga.ticex_annihilation.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.block.state.BlockState;
import saga.ticex_annihilation.config.SATCConfig;
import saga.ticex_annihilation.registries.BlockEntityRegistry;
import saga.ticex_annihilation.registries.MobEffectRegistry;

import java.util.function.Supplier;

public class NeutronJammerBlockEntity extends BaseJammerBlockEntity {
    public NeutronJammerBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.NEUTRON_JAMMER.get(), pos, state);
    }

    @Override
    protected boolean isEnabled() {
        return SATCConfig.ENABLE_NEUTRON_JAMMER.get();
    }

    @Override
    protected double getRange() {
        return SATCConfig.NEUTRON_JAMMER_RANGE.get();
    }

    @Override
    protected Supplier<MobEffect> getEffect() {
        // 先ほど修正した、解除可能なNJエフェクトを返す
        return MobEffectRegistry.NEUTRON_JAMMER_ACTIVE;
    }
}