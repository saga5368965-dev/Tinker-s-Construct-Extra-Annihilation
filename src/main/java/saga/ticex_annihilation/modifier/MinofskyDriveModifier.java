package saga.ticex_annihilation.modifier;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

import java.util.List;

/**
 * ミノフスキー・ドライブ Modifier
 * 1.20.1 Hook適合。背中に光の翼（パーティクル）を出し、接触した敵を粉砕する。
 */
public class MinofskyDriveModifier extends Modifier implements InventoryTickModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder builder) {
        super.registerHooks(builder);
        builder.addHook(this, ModifierHooks.INVENTORY_TICK);
    }

    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry modifier, Level level, LivingEntity holder, int itemSlot, boolean isSelected, boolean isCorrectSlot, ItemStack stack) {
        // 防具スロット（チェストプレート）に装備されている時のみ発動
        if (level.isClientSide || !isCorrectSlot) return;

        int levelMod = modifier.getLevel();

        // 1. 飛行能力の付与（クリエイティブ飛行と同じ挙動を強制）
        if (holder instanceof net.minecraft.world.entity.player.Player player) {
            if (!player.getAbilities().mayfly) {
                player.getAbilities().mayfly = true;
                player.onUpdateAbilities();
            }
        }

        // 2. 「光の翼」パーティクル演出
        if (level instanceof ServerLevel serverLevel && holder.tickCount % 2 == 0) {
            spawnWingParticles(serverLevel, holder);
        }

        // 3. 接触ダメージ：周囲の敵を素材攻撃力×5倍で粉砕
        executeTouchDamage(tool, levelMod, holder);
    }

    private void spawnWingParticles(ServerLevel level, LivingEntity holder) {
        Vec3 look = holder.getLookAngle().reverse(); // 背中方向
        Vec3 pos = holder.position().add(0, holder.getEyeHeight() * 0.7, 0);

        // 左右に広がる翼のようなパーティクル
        for (int i = 0; i < 5; i++) {
            double sideOffset = (i - 2) * 0.4;
            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                    pos.x + look.x * 0.3 + (holder.getBbWidth() * sideOffset),
                    pos.y + (i * 0.1),
                    pos.z + look.z * 0.3,
                    1, 0.1, 0.1, 0.1, 0.02);

            // より「翼」らしく、エンドロッドの光を混ぜる
            level.sendParticles(ParticleTypes.END_ROD,
                    pos.x + look.x * 0.2, pos.y, pos.z + look.z * 0.2,
                    1, 0.5, 0.5, 0.5, 0.01);
        }
    }

    private void executeTouchDamage(IToolStackView tool, int modifierLevel, LivingEntity holder) {
        // 判定範囲：自分の周囲2マス程度
        AABB hitBox = holder.getBoundingBox().inflate(2.0);
        List<LivingEntity> targets = holder.level().getEntitiesOfClass(LivingEntity.class, hitBox, e -> e != holder && e.isAlive());

        if (!targets.isEmpty()) {
            float baseDamage = tool.getStats().get(ToolStats.ATTACK_DAMAGE);
            // 素材攻撃力 × レベル × 5倍 の接触ダメージ
            float finalDamage = baseDamage * (modifierLevel * 5.0F);

            for (LivingEntity victim : targets) {
                // 接触するだけでダメージ。ダメージソースは本人として扱う
                victim.hurt(holder.damageSources().mobAttack(holder), finalDamage);
            }
        }
    }
}