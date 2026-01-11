package saga.ticex_annihilation.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.block.state.BlockState;
import saga.ticex_annihilation.config.SATCConfig;
import saga.ticex_annihilation.registries.BlockEntityRegistry;
import saga.ticex_annihilation.registries.MobEffectRegistry;

import java.util.function.Supplier;

public class MinofskyDiffuserBlockEntity extends BaseJammerBlockEntity {
    public MinofskyDiffuserBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.MINOFSKY_DIFFUSER.get(), pos, state);
    }

    @Override
    protected boolean isEnabled() {
        return SATCConfig.ENABLE_MINOFSKY_JAMMING.get();
    }

    @Override
    protected double getRange() {
        return SATCConfig.MINOFSKY_RANGE.get();
    }

    @Override
    protected Supplier<MobEffect> getEffect() {
        // ミノフスキー電波障害エフェクトを返す
        return MobEffectRegistry.MINOFSKY_INTERFERENCE;
    }
}