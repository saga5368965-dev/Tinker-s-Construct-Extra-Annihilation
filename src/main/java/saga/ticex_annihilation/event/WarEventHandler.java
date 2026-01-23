package saga.ticex_annihilation.event;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import saga.ticex_annihilation.TimexAnnihilation;
import saga.ticex_annihilation.item.CalamityRingItem;
import saga.ticex_annihilation.registries.EntityRegistry;

import java.util.List;

@Mod.EventBusSubscriber(modid = TimexAnnihilation.MODID)
public class WarEventHandler {

    // --- 殺害時イベント：指輪の権能をトリガーする ---
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        // 攻撃者がプレイヤーかつ、指輪を持っている場合
        if (event.getSource().getEntity() instanceof Player player) {
            ItemStack ring = player.getMainHandItem();
            if (ring.getItem() instanceof CalamityRingItem ringItem) {
                // 指輪側の殺害時処理（衝撃波・吸収・魂蓄積）を実行
                ringItem.onEntityKilled(player, event.getEntity(), ring);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        CompoundTag nbt = entity.getPersistentData();

        if (nbt.contains("CalamityCurseTicks") && nbt.getInt("CalamityCurseTicks") > 0) {
            handleCurse(entity, nbt);
        }

        if (nbt.contains("CalamityBlessingTicks") && nbt.getInt("CalamityBlessingTicks") > 0) {
            handleBlessing(entity, nbt);
        }
    }

    private static void handleCurse(LivingEntity entity, CompoundTag nbt) {
        entity.setDeltaMovement(entity.getDeltaMovement().multiply(0.3, 1.0, 0.3));
        if (entity instanceof Mob mob && (mob.getTarget() == null || mob.tickCount % 20 == 0)) {
            List<LivingEntity> targets = mob.level().getEntitiesOfClass(LivingEntity.class, mob.getBoundingBox().inflate(32));
            targets.stream().filter(e -> e != mob && e.isAlive()).findAny().ifPresent(mob::setTarget);
        }
        if (entity instanceof Player player) {
            if (player.getAbilities().flying) {
                player.getAbilities().flying = false;
                player.onUpdateAbilities();
            }
            if (player.isFallFlying()) player.stopFallFlying();
        }
        nbt.putInt("CalamityCurseTicks", nbt.getInt("CalamityCurseTicks") - 1);
    }

    private static void handleBlessing(LivingEntity entity, CompoundTag nbt) {
        entity.setDeltaMovement(entity.getDeltaMovement().multiply(1.5, 1.0, 1.5));
        // しもべのAI速度を5倍化
        if (entity.getType() == EntityRegistry.SERVANT_ENTITY.get()) {
            for (int i = 0; i < 4; i++) entity.aiStep();
        }
        if (entity instanceof Player player) player.resetAttackStrengthTicker();
        nbt.putInt("CalamityBlessingTicks", nbt.getInt("CalamityBlessingTicks") - 1);
    }

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (event.getEntity().getPersistentData().contains("CalamityBlessingTicks")) {
            event.setNewSpeed(event.getOriginalSpeed() * 5.0f);
        }
    }

    // 指輪から呼び出される衝撃波の実装
    public static void performDeathWave(Level level, Player player, Vec3 center, float radius, float damage) {
        // PlayerをLivingEntityにキャストして、下のメインロジックへ流す
        performDeathWave(level, (LivingEntity) player, center, radius, damage);
    }

    // --- メインロジック（実際の処理） ---
    public static void performDeathWave(Level level, LivingEntity caster, Vec3 center, float radius, float damage) {
        if (level.isClientSide) return;

        AABB hitBox = new AABB(center.x - radius, center.y - 2, center.z - radius, center.x + radius, center.y + 4, center.z + radius);

        // 周囲の敵をなぎ倒す
        for (Entity target : level.getEntities(caster, hitBox)) {
            if (target instanceof LivingEntity victim && victim != caster && victim.isAlive()) {
                // しもべ（支配した軍勢）は巻き込まない
                if (victim.getPersistentData().getBoolean("IsServant")) continue;

                if (target.distanceToSqr(center) <= radius * radius) {
                    // 防御無視ダメージ + 打ち上げ
                    victim.hurt(caster.damageSources().magic(), damage);
                    victim.setDeltaMovement(victim.getDeltaMovement().add(0, 0.5, 0));
                    victim.hurtMarked = true;
                }
            }
        }

        // 爆発音の追加（casterがnullでないことを確認して再生）
        level.playSound(null, center.x, center.y, center.z, SoundEvents.GENERIC_EXPLODE, caster.getSoundSource(), 1.0f, 0.5f);
    }
}