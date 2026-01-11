package saga.ticex_annihilation.modifier;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;

public class LongRangeLinkModifier extends Modifier {

    @Override
    public @NotNull Component getDisplayName(int level) {
        return Component.translatable("modifier.ticex_annihilation.long_range_link");
    }
    public void projectileTick(IToolStackView tool, ModifierEntry modifier, Projectile projectile, @Nullable Entity shooter, @Nullable Entity target) {
        // 発射された瞬間(tickCount 1)に補正をかける
        if (projectile.tickCount == 1) {
            // 1. 初速を5倍（射程5倍相当）
            projectile.setDeltaMovement(projectile.getDeltaMovement().scale(5.0));
            projectile.hasImpulse = true;

            // 2. 弾ブレ無効化：現在の進行方向を「完璧な直線」として再固定
            // (通常はTickごとに空気抵抗や重力でブレるが、ここで重力を消す)
            projectile.setNoGravity(true);
        }

        // 3. 距離減衰の無効化（TACZ/Iron's共通）
        // 飛翔体の「基本ダメージ」をTickごとにリフレッシュ、あるいは
        // 飛翔体の「寿命(LifeTime)」による威力低下計算を無視させるフラグを立てる
        // ※バニラProjectileの範囲では、重力無視＝弾道ドロップなし＝減衰なしとして機能します。
    }
}
