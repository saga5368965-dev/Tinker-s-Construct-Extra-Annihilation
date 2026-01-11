package saga.ticex_annihilation.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;

/**
 * サイコミュ（脳波伝導）システム判定用ユーティリティ
 */
public class PsycommuHandler {

    /**
     * 指定された対象（プレイヤー、Mob、投射物）が
     * サイコミュによる制御、または適正を持っているかを判定する
     */
    public static boolean isPsycommuActive(Entity entity) {
        if (entity == null) return false;

        // 1. 投射物（ファンネル等）の判定
        // エンティティが「isPsycommuControlled」というNBTタグを持っていれば、
        // ミノフスキー粒子による誘導障害を完全に無視する
        if (entity instanceof Projectile) {
            if (entity.getPersistentData().contains("isPsycommuControlled") &&
                    entity.getPersistentData().getBoolean("isPsycommuControlled")) {
                return true;
            }
        }

        // 2. プレイヤー/LivingEntityの判定
        if (entity instanceof LivingEntity living) {
            // NBTによる直接付与（ニュータイプ覚醒状態など）
            if (living.getPersistentData().getBoolean("hasPsycommuAptitude")) {
                return true;
            }

            // TODO: ここにModifier（サイコフレーム等）の所持判定を後で追加
        }

        return false;
    }
}
