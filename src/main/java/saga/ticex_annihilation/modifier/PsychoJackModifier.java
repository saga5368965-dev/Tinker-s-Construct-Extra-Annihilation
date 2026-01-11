package saga.ticex_annihilation.modifier;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.common.TinkerTags;

public class PsychoJackModifier extends Modifier {

    public PsychoJackModifier() {
        super();
        // ダメージを無効化して奪うためにForgeイベントを登録
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onProjectileImpact(LivingAttackEvent event) {
        if (event.getEntity().level().isClientSide) return;

        LivingEntity wearer = event.getEntity();
        Entity directEntity = event.getSource().getDirectEntity();

        // 1. 相手が飛び道具（Projectile）であること（TACZの弾丸やISSの魔法弾もこれに含まれる）
        if (directEntity instanceof Projectile proj) {
            // 2. 自分がこのModifierを持っているかチェック（チェストプレート想定）
            ItemStack chest = wearer.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST);
            if (chest.isEmpty() || !chest.is(TinkerTags.Items.MODIFIABLE)) return;

            if (ToolStack.from(chest).getModifierLevel(this) > 0) {
                // 3. 自分の弾でなければジャック開始
                if (proj.getOwner() != wearer) {
                    // 所有権を自分に書き換え（ISSの魔法などはこれでダメージ判定が反転する）
                    proj.setOwner(wearer);

                    // リダイレクト：攻撃者へ撃ち返す
                    Entity attacker = event.getSource().getEntity();
                    if (attacker != null) {
                        Vec3 targetPos = attacker.getEyePosition();
                        Vec3 direction = targetPos.subtract(proj.position()).normalize();
                        // 速度を1.5倍にして反射
                        double speed = proj.getDeltaMovement().length() * 1.5;
                        proj.setDeltaMovement(direction.scale(Math.max(speed, 2.0))); // ISS等の遅い弾も加速
                    } else {
                        proj.setDeltaMovement(proj.getDeltaMovement().scale(-1.5));
                    }

                    // 4. ダメージ自体はキャンセル（無効化）
                    event.setCanceled(true);

                    // 演出
                    wearer.level().playSound(null, wearer.blockPosition(),
                            SoundEvents.ZOMBIE_VILLAGER_CONVERTED, SoundSource.PLAYERS, 1.0F, 2.0F);
                }
            }
        }
    }
}