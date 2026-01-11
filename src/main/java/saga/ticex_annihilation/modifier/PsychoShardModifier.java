package saga.ticex_annihilation.modifier;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.client.TooltipKey;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.display.TooltipModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.List;

public class PsychoShardModifier extends Modifier implements InventoryTickModifierHook, TooltipModifierHook, MeleeHitModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.@NotNull Builder builder) {
        super.registerHooks(builder);
        // あなたの ModifierHooks 定義に従って登録
        builder.addHook(this, ModifierHooks.INVENTORY_TICK, ModifierHooks.TOOLTIP, ModifierHooks.MELEE_HIT);
    }

    @Override
    public @NotNull Component getDisplayName(int level) {
        return Component.translatable("modifier.ticex_annihilation.psycho_shard");
    }

    @Override
    public void addTooltip(IToolStackView tool, ModifierEntry modifier, @Nullable Player player, List<Component> tooltip, TooltipKey tooltipKey, TooltipFlag tooltipFlag) {
        tooltip.add(Component.translatable("modifier.ticex_annihilation.psycho_shard.description").withStyle(ChatFormatting.GOLD));
    }

    // 提供された .class ソースの引数に合わせて修正
    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        LivingEntity target = context.getLivingTarget();
        if (target != null && !target.level().isClientSide) {
            // 直接攻撃した相手を永続拘束
            target.getPersistentData().putBoolean("PsychoShardPermanent", true);
            // 周囲をレベルに応じた範囲で拘束 (30秒)
            applyAreaLock(target, 20.0D * modifier.getLevel(), context.getAttacker());
        }
    }

    @Override
    public void onInventoryTick(@NotNull IToolStackView tool, @NotNull ModifierEntry modifier, Level level, LivingEntity holder, int itemSlot, boolean isSelected, boolean isCorrectSlot, ItemStack stack) {
        // 装備中（isCorrectSlot）なら常に周囲をジャック
        if (!level.isClientSide && isCorrectSlot) {
            applyAreaLock(holder, 100.0D, holder);
        }
    }

    private void applyAreaLock(LivingEntity centerEntity, double radius, LivingEntity attacker) {
        AABB area = centerEntity.getBoundingBox().inflate(radius);
        List<LivingEntity> targets = centerEntity.level().getEntitiesOfClass(LivingEntity.class, area);

        for (LivingEntity entity : targets) {
            if (entity == attacker || !entity.isAlive()) continue;

            if (!entity.getPersistentData().getBoolean("PsychoShardPermanent")) {
                entity.getPersistentData().putInt("PsychoShardTimer", 600);
            }
        }
    }
}