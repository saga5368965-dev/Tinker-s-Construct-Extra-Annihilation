package saga.ticex_annihilation.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import saga.ticex_annihilation.TimexAnnihilation;
import saga.ticex_annihilation.registries.network.PacketHandler;
import saga.ticex_annihilation.registries.network.SATCPacket; // パケット名を修正

@Mod.EventBusSubscriber(modid = TimexAnnihilation.MODID, value = Dist.CLIENT)
public class ClientInputHandler {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        // GUIが開いている間は入力を無視（チャット中などに暴発しないように）
        if (net.minecraft.client.Minecraft.getInstance().screen != null) return;

        // DEPLOY_KEY (Vキー) のチェック
        if (TimexAnnihilation.DEPLOY_KEY != null && TimexAnnihilation.DEPLOY_KEY.consumeClick()) {
            // Type 0: 展開 / 回収
            PacketHandler.sendToServer(new SATCPacket(0));
        }

        // MODE_KEY (Xキー) のチェック
        if (TimexAnnihilation.MODE_KEY != null && TimexAnnihilation.MODE_KEY.consumeClick()) {
            // Type 1: モード切り替え
            PacketHandler.sendToServer(new SATCPacket(1));
        }

        // DRIVE_KEY (Bキー) のチェック (追加分)
        if (TimexAnnihilation.DRIVE_KEY != null && TimexAnnihilation.DRIVE_KEY.consumeClick()) {
            // Type 2: ドライブ発動（必要に応じて）
            PacketHandler.sendToServer(new SATCPacket(2));
        }
    }
}