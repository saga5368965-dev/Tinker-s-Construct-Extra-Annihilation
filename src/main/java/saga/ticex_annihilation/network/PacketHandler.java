package saga.ticex_annihilation.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import saga.ticex_annihilation.TimexAnnihilation; // 綴りを統一

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(TimexAnnihilation.MODID, "main"), // MODID に修正
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;
    private static int id() { return packetId++; }

    public static void init() { // register から init に名称変更（慣習的）
        CHANNEL.registerMessage(id(),
                S_A_T_C_Packet.class,
                S_A_T_C_Packet::encode,
                S_A_T_C_Packet::decode,
                S_A_T_C_Packet::handle
        );
    }

    public static void sendToServer(Object msg) {
        CHANNEL.sendToServer(msg);
    }
}
