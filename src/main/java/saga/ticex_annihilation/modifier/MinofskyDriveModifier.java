package saga.ticex_annihilation.modifier;

import saga.ticex_annihilation.registries.MobEffectRegistry;
import saga.ticex_annihilation.util.PsycommuHandler;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Team; // PlayerTeamではなくTeamを使用
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.common.TinkerTags;

public class MinofskyDriveModifier extends Modifier {
    public MinofskyDriveModifier() {
        super();
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;
        ServerPlayer player = (ServerPlayer) event.player;

        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chest.isEmpty() || !chest.is(TinkerTags.Items.MODIFIABLE)) return;

        ToolStack tool = ToolStack.from(chest);
        int level = tool.getModifierLevel(this);
        if (level <= 0) return;

        boolean isActive = player.getPersistentData().getBoolean("MinofskyDriveActive");

        if (isActive && player.getAbilities().flying) {
            // 速度設定 (1.20.1 Forge アクセサ)
            float speed = 0.15f + (level * 0.05f);
            player.getAbilities().setFlyingSpeed(speed);

            spawnMegaWings(player, level);
            handleAreaControl(player, level);
        } else {
            player.getAbilities().setFlyingSpeed(0.05f);
        }
        player.onUpdateAbilities();
    }

    private void handleAreaControl(ServerPlayer player, int level) {
        double blastRange = 10.0 + (level * 5.0);
        AABB blastArea = player.getBoundingBox().inflate(blastRange);

        float dmg = player.getArmorValue() * 5.0f;
        player.level().getEntitiesOfClass(LivingEntity.class, blastArea, e -> e != player).forEach(t -> {
            if (isAllyAndNewtype(player, t)) {
                Vec3 push = t.position().subtract(player.position()).normalize();
                t.setDeltaMovement(push.scale(1.5));
                t.hurt(player.damageSources().magic(), dmg);
                t.hurtMarked = true;
            }
        });

        player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(500)).forEach(e -> {
            if (e != player && isAllyAndNewtype(player, e)) {
                e.addEffect(new MobEffectInstance(MobEffectRegistry.MINOFSKY_INTERFERENCE.get(), 100, 0, false, false, true));
            }
        });
    }

    @SubscribeEvent
    public void onAttack(LivingAttackEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            boolean isActive = player.getPersistentData().getBoolean("MinofskyDriveActive");
            if (isActive && ToolStack.from(player.getItemBySlot(EquipmentSlot.CHEST)).getModifierLevel(this) > 0) {
                if (event.getSource().getDirectEntity() instanceof Projectile ||
                        event.getSource().is(net.minecraft.world.damagesource.DamageTypes.SONIC_BOOM)) {
                    if (event.getSource().getDirectEntity() != null) event.getSource().getDirectEntity().discard();
                    event.setCanceled(true);
                }
            }
        }
    }

    private boolean isAllyAndNewtype(ServerPlayer driveUser, LivingEntity target) {
        Team myTeam = driveUser.getTeam();
        Team targetTeam = target.getTeam();
        boolean isAlly = (myTeam != null && targetTeam != null && myTeam.isAlliedTo(targetTeam));
        return !isAlly || !PsycommuHandler.isPsycommuActive(target);
    }

    private void spawnMegaWings(ServerPlayer p, int l) {
        ServerLevel w = p.serverLevel();
        double len = 15.0 + (l * 5.0);
        Vec3 look = p.getLookAngle().scale(-1.0);
        for (double j = 0; j < len; j += 2.0) {
            double s = j * 0.7; double h = j * 0.5;
            w.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, p.getX() + look.x + (s * -look.z), p.getY() + 1.5 + h, p.getZ() + look.z + (s * look.x), 2, 0.1, 0.1, 0.1, 0.05);
            w.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, p.getX() + look.x - (s * -look.z), p.getY() + 1.5 + h, p.getZ() + look.z - (s * look.x), 2, 0.1, 0.1, 0.1, 0.05);
        }
    }
}