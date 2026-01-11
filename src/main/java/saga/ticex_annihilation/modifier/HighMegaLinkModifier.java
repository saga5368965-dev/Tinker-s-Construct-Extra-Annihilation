package saga.ticex_annihilation.modifier;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;

import java.util.List;

public class HighMegaLinkModifier extends Modifier {
    public void projectileTick(IToolStackView tool, ModifierEntry modifier, Projectile projectile, @Nullable Entity shooter, @Nullable Entity target) {
        // 壁をすり抜ける設定（物理演算を無視）
        projectile.noPhysics = true;

        // 判定の巨大化：15ティック（約0.75秒）ごとに周囲の敵をスキャンしてダメージ
        // この範囲が5倍（半径約2.5〜3.0ブロック）になるイメージ
        if (projectile.tickCount % 2 == 0) {
            Level level = projectile.level();
            AABB area = projectile.getBoundingBox().inflate(2.5D); // 元の判定を2.5倍に広げる（直径5ブロック分）
            List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area);

            for (LivingEntity victim : entities) {
                if (victim != shooter) {
                    // 攻撃力を5倍に増幅して直接与える（魔法属性ダメージ扱い）
                    float baseDamage = 10.0F; // ベース威力
                    victim.hurt(projectile.damageSources().magic(), baseDamage * 5.0F);
                }
            }
        }

        // 弾が奈落に落ち続けないよう、一定時間で消去
        if (projectile.tickCount > 100) {
            projectile.discard();
        }
    }
}
