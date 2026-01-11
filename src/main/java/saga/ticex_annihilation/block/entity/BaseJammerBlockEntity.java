package saga.ticex_annihilation.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import java.util.function.Supplier;

public abstract class BaseJammerBlockEntity extends BlockEntity {
    public BaseJammerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /**
     * BlockクラスのgetTickerから呼ばれる処理
     */
    public static <T extends BaseJammerBlockEntity> void tick(Level level, BlockPos pos, BlockState state, T blockEntity) {
        // サーバー側のみ、かつ20ティック(1秒)に1回実行して負荷を軽減
        if (level.isClientSide || level.getGameTime() % 20 != 0) return;

        // コンフィグ等で有効化されているかチェック
        if (!blockEntity.isEnabled()) return;

        double range = blockEntity.getRange();
        MobEffect effect = blockEntity.getEffect().get();

        // 指定範囲内のLivingEntity（プレイヤー、モブ、NPCなど）をすべて取得
        AABB area = new AABB(pos).inflate(range);

        level.getEntitiesOfClass(LivingEntity.class, area).forEach(entity -> {
            // エフェクト付与：120ティック(6秒)
            // 範囲内にいる限り1秒ごとに「残り6秒」へ上書きされるため、表示上は維持される。
            // 範囲外に出た、あるいはブロックを破壊した場合は上書きが止まり、6秒後に自動消滅する。
            entity.addEffect(new MobEffectInstance(
                    effect,
                    120,    // 持続時間（6秒）
                    0,      // 増幅
                    false,  // アンビエント（環境由来か）
                    false,  // 粒子（エフェクトのモヤモヤ）
                    true    // 右上のアイコン表示
            ));
        });
    }

    protected abstract boolean isEnabled();
    protected abstract double getRange();
    protected abstract Supplier<MobEffect> getEffect();
}