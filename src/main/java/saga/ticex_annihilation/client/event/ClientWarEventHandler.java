package saga.ticex_annihilation.client.event;

import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import saga.ticex_annihilation.TimexAnnihilation;

@Mod.EventBusSubscriber(modid = TimexAnnihilation.MODID, value = Dist.CLIENT)
public class ClientWarEventHandler {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        // --- 戦禍の呪縛によるマップ封印ロジック ---
        if (player.getPersistentData().contains("CalamityCurseTicks") &&
                player.getPersistentData().getInt("CalamityCurseTicks") > 0) {

            // 1. 外部MOD対策：盲目エフェクトを「強制注入」
            // 期間が1秒(20ticks)でも、毎チック上書きされるため、牛乳で消しても次の瞬間には復活する
            if (!player.hasEffect(MobEffects.BLINDNESS)) {
                player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0, false, false, false));
            }

            // 2. HUDの妨害
            // 視界をさらに悪化させる、あるいは砂嵐のようなパーティクルを出す処理をここに
        }
    }

    // --- GUI（ミニマップ等）のレンダリング阻害 ---
    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Pre event) {
        Player player = Minecraft.getInstance().player;
        if (player != null && player.getPersistentData().contains("CalamityCurseTicks")) {
            if (player.getPersistentData().getInt("CalamityCurseTicks") > 0) {
                // 特定のHUD要素（マップなど）を非表示にするためのフラグ操作
                // 多くのMODはBlindness状態で自律的に消えるが、
                // ここで特定のオーバーレイをキャンセルすることも可能
            }
        }
    }
}
