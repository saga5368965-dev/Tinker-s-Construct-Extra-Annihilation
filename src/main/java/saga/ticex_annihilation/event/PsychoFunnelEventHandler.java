package saga.ticex_annihilation.event;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import saga.ticex_annihilation.TimexAnnihilation;
import saga.ticex_annihilation.registries.ModifierRegistry;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

@Mod.EventBusSubscriber(modid = TimexAnnihilation.MODID)
public class PsychoFunnelEventHandler {

    @SubscribeEvent
    public static void onProjectileTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity().level().isClientSide) return;

        LivingEntity shooter = event.getEntity();

        // 武器の特性レベルを確認
        ToolStack tool = ToolStack.from(shooter.getMainHandItem());
        int level = tool.getModifierLevel(ModifierRegistry.PSYCHO_FUNNEL.get());

        if (level > 0) {
            // 索敵範囲を大幅拡大（128ブロック：ほぼ描画距離内すべて）
            double range = 128.0D;
            shooter.level().getEntitiesOfClass(Projectile.class, shooter.getBoundingBox().inflate(range),
                    p -> p.getOwner() == shooter).forEach(projectile -> {

                // 発射から少し経ってから追尾開始（ギュン！という加速感を出すため）
                if (projectile.tickCount > 1) {
                    ModifierRegistry.PSYCHO_FUNNEL.get().executeStrongHoming(projectile, shooter, level);

                    // 「ピキーン！」というニュータイプ覚醒音の演出
                    // 経験値オーブの音を最高ピッチにするとそれっぽくなります
                    if (projectile.tickCount % 10 == 0) {
                        projectile.level().playSound(null, projectile.getX(), projectile.getY(), projectile.getZ(),
                                SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.NEUTRAL, 0.5F, 2.0F);
                    }
                }
            });
        }
    }
}