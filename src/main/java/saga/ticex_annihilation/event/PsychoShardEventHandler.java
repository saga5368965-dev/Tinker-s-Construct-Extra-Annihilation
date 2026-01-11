package saga.ticex_annihilation.event;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import saga.ticex_annihilation.TimexAnnihilation;

@Mod.EventBusSubscriber(modid = TimexAnnihilation.MODID)
public class PsychoShardEventHandler {

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity == null || entity.level().isClientSide) return;

        CompoundTag data = entity.getPersistentData();

        // 「永続フラグ」があるか、または「タイマー」が残っているかチェック
        boolean isPermanent = data.getBoolean("PsychoShardPermanent");
        boolean hasTimer = data.contains("PsychoShardTimer");

        if (isPermanent || hasTimer) {
            int timer = data.getInt("PsychoShardTimer");

            // --- 拘束ロジック (絶対固定) ---
            entity.setDeltaMovement(Vec3.ZERO);
            entity.setNoGravity(true);
            entity.hasImpulse = true;

            // AIの完全停止
            if (entity instanceof Mob mob) {
                mob.setNoAi(true);
            }

            // --- 演出 (オレンジ色の裁き) ---
            if (entity.tickCount % 4 == 0 && entity.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.FLAME,
                        entity.getX(), entity.getY() + 0.1, entity.getZ(),
                        8, 0.5, 0.1, 0.5, 0.05);
            }

            // タイマー処理 (永続でない場合のみカウントダウン)
            if (hasTimer && !isPermanent) {
                if (timer > 0) {
                    data.putInt("PsychoShardTimer", timer - 1);
                } else {
                    // カウント終了時の解放処理
                    if (entity instanceof Mob mob) {
                        mob.setNoAi(false);
                    }
                    entity.setNoGravity(false);
                    data.remove("PsychoShardTimer");
                }
            }
        }
    }
}