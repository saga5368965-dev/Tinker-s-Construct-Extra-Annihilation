package saga.ticex_annihilation.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import saga.ticex_annihilation.TimexAnnihilation;
import saga.ticex_annihilation.network.PacketHandler;
import saga.ticex_annihilation.network.S_A_T_C_Packet;

@Mod.EventBusSubscriber(modid = TimexAnnihilation.MODID, value = Dist.CLIENT)
public class ClientInputHandler {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        // メインクラスで定義した DEPLOY_KEY (Vキー) をチェック
        if (TimexAnnihilation.DEPLOY_KEY != null && TimexAnnihilation.DEPLOY_KEY.consumeClick()) {
            // Type 0: 展開 / 回収
            PacketHandler.sendToServer(new S_A_T_C_Packet(0));
        }

        // メインクラスで定義した MODE_KEY (Xキー) をチェック
        if (TimexAnnihilation.MODE_KEY != null && TimexAnnihilation.MODE_KEY.consumeClick()) {
            // Type 1: モード切り替え
            PacketHandler.sendToServer(new S_A_T_C_Packet(1));
        }
    }
}
