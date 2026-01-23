package saga.ticex_annihilation.registries.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import saga.ticex_annihilation.item.SATCItem;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * クライアントからのキー入力をサーバーへ伝えるパケット
 */
public record SATCPacket(int type) {

    public static SATCPacket decode(FriendlyByteBuf buf) {
        return new SATCPacket(buf.readInt());
    }

    public static void encode(SATCPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.type);
    }

    public static void handle(SATCPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            // 1. SATCItem側の static メソッドを呼び出してアクティブなアイテムを探す
            Optional<ItemStack> activeStack = SATCItem.findActiveSATC(player);

            activeStack.ifPresent(stack -> {
                if (stack.getItem() instanceof SATCItem item) {
                    // type に応じて機能を実行
                    switch (msg.type) {
                        case 0 -> item.toggleFunnels(stack, player); // Vキー: 展開/回収
                        case 1 -> item.cycleMode(stack, player);     // Xキー: モード変更
                        // case 2 -> item.activateDrive(stack, player); // Bキー用（必要なら追加）
                    }
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }
}