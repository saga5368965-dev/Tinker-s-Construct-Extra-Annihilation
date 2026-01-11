package saga.ticex_annihilation.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import saga.ticex_annihilation.item.SATCItem;

import java.util.function.Supplier;

public record S_A_T_C_Packet(int type) {
    // コンストラクタで 0 か 1 を受け取る

    public static S_A_T_C_Packet decode(FriendlyByteBuf buf) {
        return new S_A_T_C_Packet(buf.readInt());
    }

    public static void encode(S_A_T_C_Packet msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.type);
    }

    public static void handle(S_A_T_C_Packet msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            // 装備中（手またはCurios）のSATCを探す
            SATCItem.findActiveSATC(player).ifPresent(stack -> {
                SATCItem item = (SATCItem) stack.getItem();

                if (msg.type == 0) {
                    // Type 0: 展開 / 回収
                    item.toggleFunnels(stack, player);
                } else if (msg.type == 1) {
                    // Type 1: モード切り替え
                    item.cycleMode(stack, player);
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }
}