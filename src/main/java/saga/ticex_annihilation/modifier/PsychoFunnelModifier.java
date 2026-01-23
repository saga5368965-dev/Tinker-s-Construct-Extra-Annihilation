package saga.ticex_annihilation.modifier;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import java.util.Comparator;

/**
 * サイコミュ・ファンネル (Forgeイベント登録方式)
 * 外部のEventHandlerからも呼び出し可能な「強追尾」ロジックを搭載
 */
public class PsychoFunnelModifier extends Modifier {

    public PsychoFunnelModifier() {
        super();
        // 自身でもイベントを監視（手持ち武器の直接制御用）
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onEntityTick(net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent event) {
        LivingEntity shooter = event.getEntity();
        if (shooter.level().isClientSide || shooter.tickCount % 2 != 0) return;

        // 手持ち武器にModifierがある場合、周囲64ブロックの自分の弾を制御
        int level = getModifierLevel(shooter);
        if (level > 0) {
            controlFunnels(shooter, level);
        }
    }

    private int getModifierLevel(LivingEntity entity) {
        var stack = entity.getMainHandItem();
        if (!stack.isEmpty()) {
            try {
                // IModifiableDisplayなどのチェックを含めた安全な取得
                return ToolStack.from(stack).getModifierLevel(this);
            } catch (Exception ignored) {}
        }
        return 0;
    }

    private void controlFunnels(LivingEntity shooter, int level) {
        double controlRange = 64.0D;
        AABB area = shooter.getBoundingBox().inflate(controlRange);

        // 自分がオーナーである全てのProjectileを探して追尾実行
        shooter.level().getEntitiesOfClass(Projectile.class, area, p -> p.getOwner() == shooter)
                .forEach(proj -> executeStrongHoming(proj, shooter, level));
    }

    /**
     * 強力なホーミングロジック。
     * publicに変更したことで、128ブロック索敵用のEventHandlerからも呼び出し可能。
     */
    public void executeStrongHoming(Projectile projectile, LivingEntity shooter, int level) {
        // 索敵範囲もModifierレベルに応じて強化可能 (基本100 + レベル毎に加算)
        double searchRange = 100.0D + (level * 10.0D);
        AABB searchArea = projectile.getBoundingBox().inflate(searchRange);

        LivingEntity target = projectile.level().getEntitiesOfClass(LivingEntity.class, searchArea,
                        e -> e != shooter && e.isAlive() && !e.isAlliedTo(shooter))
                .stream()
                .min(Comparator.comparingDouble(projectile::distanceToSqr))
                .orElse(null);

        if (target != null) {
            Vec3 targetPos = target.getBoundingBox().getCenter();
            Vec3 pathToTarget = targetPos.subtract(projectile.position()).normalize();
            Vec3 currentVelocity = projectile.getDeltaMovement();
            double speed = currentVelocity.length();

            if (speed > 0.1) {
                // 旋回性能（1.0に近いほど鋭い）。レベルに応じて機動性が上がる
                double turnSharpness = Math.min(0.95 + (level * 0.01), 0.99);

                Vec3 newVelocity = currentVelocity.normalize().scale(1.0 - turnSharpness)
                        .add(pathToTarget.scale(turnSharpness))
                        .normalize()
                        .scale(speed * (1.0 + (0.02 * level))); // レベルに応じて加速

                projectile.setDeltaMovement(newVelocity);

                // パーティクル演出 (エンドロッドの光が糸を引くように追尾)
                if (projectile.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.END_ROD,
                            projectile.getX(), projectile.getY(), projectile.getZ(),
                            1, 0, 0, 0, 0.01);
                }
            }
        }
    }
}