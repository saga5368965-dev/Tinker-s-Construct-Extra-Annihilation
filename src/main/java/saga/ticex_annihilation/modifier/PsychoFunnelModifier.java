package saga.ticex_annihilation.modifier;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.Comparator;

public class PsychoFunnelModifier extends Modifier {

    /**
     * 発射物が飛んでいる間、毎チック呼び出される処理
     */
    public void projectileTick(IToolStackView tool, ModifierEntry modifier, Projectile projectile, @Nullable Entity shooter, @Nullable Entity target) {
        if (shooter instanceof LivingEntity livingShooter) {
            executeStrongHoming(projectile, livingShooter, modifier.getLevel());
        }
    }

    /**
     * 強力なホーミングロジック (ギュン！と曲がる処理)
     */
    public void executeStrongHoming(Projectile projectile, LivingEntity shooter, int level) {
        // クライアントサイドでは実行しない（サーバーで計算）
        if (projectile.level().isClientSide) return;

        // 1. ターゲット検索 (半径100マスの広域スキャン)
        double searchRange = 100.0D;
        AABB area = projectile.getBoundingBox().inflate(searchRange);

        LivingEntity target = projectile.level().getEntitiesOfClass(LivingEntity.class, area,
                        e -> e != shooter && e.isAlive() && !e.isAlliedTo(shooter))
                .stream()
                .min(Comparator.comparingDouble(projectile::distanceToSqr))
                .orElse(null);

        // 2. ターゲットが見つかった場合の追尾処理
        if (target != null) {
            Vec3 targetPos = target.getBoundingBox().getCenter();
            Vec3 pathToTarget = targetPos.subtract(projectile.position()).normalize();

            Vec3 currentVelocity = projectile.getDeltaMovement();
            double speed = currentVelocity.length();

            // スピードが極端に遅い場合は追尾させない
            if (speed > 0.1) {
                // 【ギュン！ポイント】旋回性能を極限(0.98)に設定
                // ほぼ慣性を無視してターゲットへ直進する
                double turnSharpness = 0.98;
                Vec3 newVelocity = currentVelocity.normalize().scale(1.0 - turnSharpness)
                        .add(pathToTarget.scale(turnSharpness))
                        .normalize()
                        .scale(speed * 1.02); // 追尾中に2%ずつ加速(絶望感の演出)

                projectile.setDeltaMovement(newVelocity);

                // 3. 演出面 (ニュータイプ音と白い光)

                // 「ピキーン！」という覚醒音 (5チックに1回)
                if (projectile.tickCount % 5 == 0) {
                    projectile.level().playSound(null, projectile.getX(), projectile.getY(), projectile.getZ(),
                            SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.NEUTRAL, 0.5F, 2.0F);
                }

                // 「シュシュシュン」という加速音 (15チックに1回)
                if (projectile.tickCount % 15 == 0) {
                    projectile.level().playSound(null, projectile.getX(), projectile.getY(), projectile.getZ(),
                            SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.NEUTRAL, 0.3F, 1.8F);
                }

                // 白い光の尾 (End Rodパーティクル)
                if (projectile.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.END_ROD,
                            projectile.getX(), projectile.getY(), projectile.getZ(),
                            1, 0, 0, 0, 0.02);
                }
            }
        }
    }
}