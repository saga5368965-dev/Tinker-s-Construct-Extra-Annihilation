package saga.ticex_annihilation.modifier;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

public class LongRangeLinkModifier extends Modifier {

    public LongRangeLinkModifier() {
        super();
        // TiCのフックを使わず、Forgeのイベントバスに直接登録して飛翔体を監視する
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public @NotNull Component getDisplayName(int level) {
        return Component.translatable("modifier.ticex_annihilation.long_range_link");
    }

    /**
     * エンティティが世界に出現した瞬間のイベント
     */
    @SubscribeEvent
    public void onProjectileSpawn(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide) return;

        // 1. 出現したのが飛び道具（Projectile）かチェック
        if (event.getEntity() instanceof Projectile proj) {
            Entity shooter = proj.getOwner();

            // 2. 撃った主（Shooter）がLivingEntityであること
            if (shooter instanceof net.minecraft.world.entity.LivingEntity livingShooter) {
                // 3. メインハンドの武器にこのModifierがついているかチェック
                ItemStack held = livingShooter.getMainHandItem();
                if (held.isEmpty() || !held.is(TinkerTags.Items.MODIFIABLE)) return;

                if (ToolStack.from(held).getModifierLevel(this) > 0) {
                    // 4. 初速を5倍にし、重力を無効化（長距離リンク発動）
                    proj.setDeltaMovement(proj.getDeltaMovement().scale(5.0));
                    proj.hasImpulse = true;
                    proj.setNoGravity(true);

                    // ※この方式なら tickCount 1 を待たずに「生成された瞬間」に書き換え可能
                }
            }
        }
    }
}