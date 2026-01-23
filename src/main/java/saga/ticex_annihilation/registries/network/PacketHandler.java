package saga.ticex_annihilation.registries.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import saga.ticex_annihilation.TimexAnnihilation;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";

    // チャンネルの定義。MODID を介して一意の通信路を確保
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(TimexAnnihilation.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;
    private static int id() { return packetId++; }

    /**
     * FMLCommonSetupEvent で呼び出されるパケットの登録処理
     */
    public static void init() {
        // パケット名をこれまでの修正に合わせ SATCPacket に統一
        CHANNEL.registerMessage(id(),
                SATCPacket.class,
                SATCPacket::encode,
                SATCPacket::decode,
                SATCPacket::handle
        );

        // ★ 追加: 軍勢選択用パケット (ID 1)
        CHANNEL.registerMessage(id(),
                SelectServantPacket.class,
                SelectServantPacket::encode,
                SelectServantPacket::decode,
                SelectServantPacket::handle
        );
        CHANNEL.registerMessage(id(),
                OpenCalamityGuiPacket.class,
                OpenCalamityGuiPacket::encode,
                OpenCalamityGuiPacket::decode,
                OpenCalamityGuiPacket::handle
        );
    }

    /**
     * クライアントからサーバーへパケットを送信
     */
    public static void sendToServer(Object msg) {
        CHANNEL.sendToServer(msg);
    }
}