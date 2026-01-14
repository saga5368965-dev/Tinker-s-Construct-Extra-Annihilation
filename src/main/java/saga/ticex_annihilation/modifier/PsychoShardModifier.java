package saga.ticex_annihilation.modifier;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.List;

public class PsychoShardModifier extends Modifier implements InventoryTickModifierHook, MeleeHitModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.@NotNull Builder builder) {
        super.registerHooks(builder);
        // TooltipHookを削除し、TickとHitのみ登録
        builder.addHook(this, ModifierHooks.INVENTORY_TICK, ModifierHooks.MELEE_HIT);
    }

    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        LivingEntity target = context.getLivingTarget();
        if (target != null && !target.level().isClientSide) {
            // 直接攻撃した相手を永続拘束（フラグ付与）
            target.getPersistentData().putBoolean("PsychoShardPermanent", true);

            // 攻撃時の波及範囲: レベル1で100m、レベル2で200m...
            // 殴った瞬間にマップの広範囲が静止するイメージ
            double burstRadius = 100.0D * modifier.getLevel();
            applyAreaLock(target, burstRadius, context.getAttacker());
        }
    }

    @Override
    public void onInventoryTick(@NotNull IToolStackView tool, @NotNull ModifierEntry modifier, Level level, LivingEntity holder, int itemSlot, boolean isSelected, boolean isCorrectSlot, ItemStack stack) {
        // 装備中なら常に周囲を支配（負荷軽減のため2秒に1回実行）
        if (!level.isClientSide && isCorrectSlot && holder.tickCount % 40 == 0) {
            // パッシブ範囲: レベル1で100m、レベルごとに+50m
            // 装備しているだけで半径100m以上の敵が自動的にジャックされる
            double passiveRadius = 100.0D + (modifier.getLevel() - 1) * 50.0D;
            applyAreaLock(holder, passiveRadius, holder);
        }
    }

    private void applyAreaLock(LivingEntity centerEntity, double radius, LivingEntity attacker) {
        // 検索範囲を拡張
        AABB area = centerEntity.getBoundingBox().inflate(radius);
        List<LivingEntity> targets = centerEntity.level().getEntitiesOfClass(LivingEntity.class, area);

        for (LivingEntity entity : targets) {
            // 自身や無敵の存在を除外
            if (entity == attacker || !entity.isAlive() || entity.isInvulnerable()) continue;

            // 永続フラグがない場合のみ、30秒のタイマーを上書きし続ける
            if (!entity.getPersistentData().getBoolean("PsychoShardPermanent")) {
                entity.getPersistentData().putInt("PsychoShardTimer", 600);
            }
        }
    }
}