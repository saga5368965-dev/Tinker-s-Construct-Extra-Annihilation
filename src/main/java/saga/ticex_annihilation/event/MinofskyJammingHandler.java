package saga.ticex_annihilation.event;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import saga.ticex_annihilation.TimexAnnihilation;
import saga.ticex_annihilation.registries.MobEffectRegistry;
import saga.ticex_annihilation.util.PsycommuHandler;

@Mod.EventBusSubscriber(modid = TimexAnnihilation.MODID)
public class MinofskyJammingHandler {

    /**
     * ① 遠隔アクセス封印 (AE2, Refined Storage, etc.)
     * 通信パケットが粒子によって遮断される演出。
     */
    @SubscribeEvent
    public static void onWirelessTerminalUse(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        if (player.hasEffect(MobEffectRegistry.MINOFSKY_INTERFERENCE.get())) {
            // サイコミュ適正者は脳波通信のため例外
            if (PsycommuHandler.isPsycommuActive(player)) return;

            String itemName = event.getItemStack().getItem().getDescriptionId().toLowerCase();
            // あらゆるModの遠隔通信端末をキーワードで網羅
            if (itemName.contains("wireless") || itemName.contains("terminal") ||
                    itemName.contains("remote") || itemName.contains("storage_network")) {

                event.setCanceled(true);
                if (player.level().isClientSide) {
                    player.displayClientMessage(Component.literal("§c[SYSTEM ERROR] ミノフスキー粒子による強い干渉：ネットワーク接続に失敗しました。"), true);
                }
            }
        }
    }

    /**
     * ② レーダー・ミニマップ・誘導・スカルク封じ
     * 周囲15mの投射物の誘導を狂わせ、自身の探知を拒絶する。
     */
    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.hasEffect(MobEffectRegistry.MINOFSKY_INTERFERENCE.get())) {

            // 1. 誘導兵器の無効化（サイコミュ以外）
            entity.level().getEntitiesOfClass(Projectile.class, entity.getBoundingBox().inflate(15.0)).forEach(proj -> {
                if (!PsycommuHandler.isPsycommuActive(proj)) {
                    // 軌道をデタラメに逸らす
                    Vec3 noise = new Vec3(
                            (Math.random() - 0.5) * 0.4,
                            (Math.random() - 0.5) * 0.4,
                            (Math.random() - 0.5) * 0.4
                    );
                    proj.setDeltaMovement(proj.getDeltaMovement().add(noise).scale(0.8));
                }
            });

            // 2. レーダー・発光探知の拒絶
            if (entity.hasGlowingTag()) {
                entity.setGlowingTag(false); // 発光を強制OFF
            }

            // 3. スカルク（振動）への干渉
            // ※スニーク状態でなくても振動を発生させにくくする等の処理
        }
    }
}
