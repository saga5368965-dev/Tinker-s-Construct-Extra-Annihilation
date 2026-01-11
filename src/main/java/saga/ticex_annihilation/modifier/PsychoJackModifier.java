package saga.ticex_annihilation.modifier;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.tools.context.EquipmentContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

public class PsychoJackModifier extends Modifier {

    @Override
    public @NotNull Component getDisplayName(int level) {
        return Component.translatable("modifier.ticex_annihilation.psycho_jack");
    }
    public void onAttacked(IToolStackView tool, ModifierEntry modifier, EquipmentContext context, net.minecraft.world.damagesource.DamageSource damageSource, float amount) {
        Entity projectile = damageSource.getDirectEntity();
        LivingEntity wearer = context.getEntity();

        if (projectile instanceof Projectile proj && proj.getOwner() != wearer) {
            if (!wearer.level().isClientSide) {
                // 1. サイコミュ・ジャック（所有権奪取）
                proj.setOwner(wearer);

                // 2. 敵の座標へ正確にリダイレクト
                Entity attacker = damageSource.getEntity();
                if (attacker != null) {
                    Vec3 targetPos = attacker.getEyePosition();
                    Vec3 direction = targetPos.subtract(proj.position()).normalize();
                    double speed = proj.getDeltaMovement().length() * 1.5;
                    proj.setDeltaMovement(direction.scale(speed));
                } else {
                    proj.setDeltaMovement(proj.getDeltaMovement().scale(-1.2));
                }

                // 3. 演出：ニュータイプ音
                wearer.level().playSound(null, wearer.getX(), wearer.getY(), wearer.getZ(),
                        SoundEvents.ZOMBIE_VILLAGER_CONVERTED, SoundSource.PLAYERS, 1.0F, 2.0F);
            }
        }
    }
}