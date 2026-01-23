package saga.ticex_annihilation.event;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import saga.ticex_annihilation.TimexAnnihilation;
import saga.ticex_annihilation.registries.ModifierRegistry;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

@Mod.EventBusSubscriber(modid = TimexAnnihilation.MODID)
public class PsychoFunnelEventHandler {

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity().level().isClientSide) return;

        LivingEntity shooter = event.getEntity();
        ItemStack held = shooter.getMainHandItem();

        // 1. 手持ちがTiCツールであることを安全に確認
        if (held.isEmpty() || !held.is(TinkerTags.Items.MODIFIABLE)) return;

        ToolStack tool = ToolStack.from(held);
        int level = tool.getModifierLevel(ModifierRegistry.PSYCHO_FUNNEL.get());

        if (level > 0) {
            // 2. 索敵範囲: 128ブロック
            double range = 128.0D;
            shooter.level().getEntitiesOfClass(Projectile.class, shooter.getBoundingBox().inflate(range),
                    p -> p.getOwner() == shooter).forEach(projectile -> {

                // 3. 追尾ロジックの呼び出し (Modifier側のメソッドをpublicにすること)
                if (projectile.tickCount > 1) {
                    ModifierRegistry.PSYCHO_FUNNEL.get().executeStrongHoming(projectile, shooter, level);

                    // 4. 「ピキーン！」という覚醒音演出 (10tickに1回)
                    if (projectile.tickCount % 10 == 0) {
                        projectile.level().playSound(null, projectile.getX(), projectile.getY(), projectile.getZ(),
                                SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.NEUTRAL, 0.3F, 2.0F);
                    }
                }
            });
        }
    }
}