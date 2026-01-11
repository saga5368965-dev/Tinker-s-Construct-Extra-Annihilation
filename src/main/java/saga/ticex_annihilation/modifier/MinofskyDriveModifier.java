package saga.ticex_annihilation.modifier;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.ToolStats;
import slimeknights.tconstruct.common.TinkerTags;

import java.util.List;

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
            // 高速飛行設定
            float speed = 0.15f + (level * 0.05f);
            player.getAbilities().setFlyingSpeed(speed);

            // 【追加】光の翼による接触攻撃ロジック
            handleWingContactDamage(player, tool, level);
        } else {
            player.getAbilities().setFlyingSpeed(0.05f);
        }
        player.onUpdateAbilities();
    }

    /**
     * 光の翼の範囲内にいる敵にダメージとノックバックを与える
     */
    private void handleWingContactDamage(ServerPlayer player, ToolStack tool, int level) {
        // 攻撃範囲：レベルに応じて拡大
        double range = 4.0 + (level * 2.0);
        AABB area = player.getBoundingBox().inflate(range);

        // ツール素材の攻撃力を参照
        float baseAttackDamage = tool.getStats().get(ToolStats.ATTACK_DAMAGE);
        // 防御力×5ダメージを追加
        float totalDamage = baseAttackDamage + (player.getArmorValue() * 5.0f);

        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && e.isAlive() && !e.isAlliedTo(player));

        for (LivingEntity target : targets) {
            // ダメージ付与（属性はプレイヤー攻撃扱い）
            if (target.hurt(player.damageSources().playerAttack(player), totalDamage)) {
                // 強烈なノックバック（外側へ弾き飛ばす）
                Vec3 knockback = target.position().subtract(player.position()).normalize().scale(1.5 + (level * 0.5));
                target.setDeltaMovement(knockback);
                target.hurtMarked = true;
            }
        }
    }

    @SubscribeEvent
    public void onAttack(LivingAttackEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            boolean isActive = player.getPersistentData().getBoolean("MinofskyDriveActive");
            if (isActive) {
                ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
                if (!chest.isEmpty() && ToolStack.from(chest).getModifierLevel(this) > 0) {
                    // I-フィールド（射撃無効）
                    if (event.getSource().getDirectEntity() instanceof Projectile) {
                        event.getSource().getDirectEntity().discard();
                        event.setCanceled(true);
                    }
                }
            }
        }
    }
}