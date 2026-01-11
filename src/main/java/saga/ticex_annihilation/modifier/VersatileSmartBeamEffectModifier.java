package saga.ticex_annihilation.modifier;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

import java.util.List;

/**
 * ヴェスバー(VSBR) Modifier
 * 誘導を廃止し、素材の攻撃力を5倍に増幅して貫通・加速する。
 */
public class VersatileSmartBeamEffectModifier extends Modifier implements InventoryTickModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.@NotNull Builder builder) {
        super.registerHooks(builder);
        // インベントリチック（装備時更新）フックを登録
        builder.addHook(this, ModifierHooks.INVENTORY_TICK);
    }

    @Override
    public void onInventoryTick(@NotNull IToolStackView tool, @NotNull ModifierEntry modifier, Level level, LivingEntity holder, int itemSlot, boolean isSelected, boolean isCorrectSlot, ItemStack stack) {
        if (level.isClientSide || !isSelected) return;

        // 自分がオーナーである弾丸をスキャン
        AABB searchArea = holder.getBoundingBox().inflate(128);
        List<Projectile> myProjectiles = level.getEntitiesOfClass(Projectile.class, searchArea, p -> p.getOwner() == holder);

        for (Projectile projectile : myProjectiles) {
            executeVsbrPhysics(tool, modifier, projectile, holder);
        }
    }

    /**
     * VSBRのメインロジック：素材攻撃力×5の貫通ダメージと加速
     */
    private void executeVsbrPhysics(IToolStackView tool, ModifierEntry modifier, Projectile projectile, LivingEntity shooter) {
        // 壁をすり抜ける（高出力ビームの表現）
        projectile.noPhysics = true;

        // 弾丸の周囲にダメージ判定を発生させる
        AABB hitBox = projectile.getBoundingBox().inflate(0.5);
        List<LivingEntity> targets = projectile.level().getEntitiesOfClass(LivingEntity.class, hitBox, e -> e != shooter && e.isAlive());

        if (!targets.isEmpty()) {
            int level = modifier.getLevel();
            // 素材の基本攻撃力を取得
            float baseDamage = tool.getStats().get(ToolStats.ATTACK_DAMAGE);

            // 【素材攻撃力 × レベル × 5倍】のダメージ
            float finalDamage = baseDamage * (level * 5.0F);

            for (LivingEntity victim : targets) {
                // プレイヤーの攻撃としてダメージを与える（素材の特性を乗せるため）
                if (victim.hurt(projectile.damageSources().mobAttack(shooter), finalDamage)) {
                    // ヒット（貫通）するたびに弾丸がさらに5%加速
                    projectile.setDeltaMovement(projectile.getDeltaMovement().scale(1.05));
                }
            }
        }

        // 寿命設定（100ティックで消去）
        if (projectile.tickCount > 100) {
            projectile.discard();
        }
    }
}
