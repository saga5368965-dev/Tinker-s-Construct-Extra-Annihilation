package saga.ticex_annihilation.modifier;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

import java.util.List;

/**
 * ハイメガ・リンク Modifier
 * PROJECTILE_TICK が存在しない環境のため、INVENTORY_TICK から弾丸を制御する
 */
public class HighMegaLinkModifier extends Modifier implements InventoryTickModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder builder) {
        super.registerHooks(builder);
        // あなたのソースにある INVENTORY_TICK を使用
        builder.addHook(this, ModifierHooks.INVENTORY_TICK);
    }

    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry modifier, Level level, LivingEntity holder, int itemSlot, boolean isSelected, boolean isCorrectSlot, ItemStack stack) {
        // サーバーサイドかつ、手に持っている時のみ実行
        if (level.isClientSide || !isSelected) return;

        // 1. 周囲 128 マスにある自分の放った弾丸（Projectile）を探す
        AABB searchArea = holder.getBoundingBox().inflate(128);
        List<Projectile> myProjectiles = level.getEntitiesOfClass(Projectile.class, searchArea, p -> p.getOwner() == holder);

        for (Projectile projectile : myProjectiles) {
            // ハイメガのメインロジックを実行
            applyHighMegaEffect(tool, modifier, projectile, holder);
        }
    }

    private void applyHighMegaEffect(IToolStackView tool, ModifierEntry modifier, Projectile projectile, LivingEntity shooter) {
        // 壁すり抜け
        projectile.noPhysics = true;

        int level = modifier.getLevel();

        // 2ティックに1回判定
        if (projectile.tickCount % 2 == 0) {
            // 素材攻撃力を取得
            float baseDamage = tool.getStats().get(ToolStats.ATTACK_DAMAGE);

            // 範囲：レベルごとに 2.5 * レベル
            double range = 2.5D * level;
            AABB hitArea = projectile.getBoundingBox().inflate(range);

            // ダメージ：素材攻撃力 × レベル × 5倍
            float finalDamage = baseDamage * (level * 5.0F);

            List<LivingEntity> targets = projectile.level().getEntitiesOfClass(LivingEntity.class, hitArea, e -> e != shooter);
            for (LivingEntity victim : targets) {
                // 特性を乗せるために mobAttack を使用
                victim.hurt(projectile.damageSources().mobAttack(shooter), finalDamage);
            }
        }

        // 5秒で消滅
        if (projectile.tickCount > 100) {
            projectile.discard();
        }
    }
}