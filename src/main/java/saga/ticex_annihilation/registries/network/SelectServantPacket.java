package saga.ticex_annihilation.registries.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import saga.ticex_annihilation.item.CalamityRingItem;

import java.util.function.Supplier;

public class SelectServantPacket {
    private final String entityId;

    public SelectServantPacket(String entityId) {
        this.entityId = entityId;
    }

    // データの書き込み
    public static void encode(SelectServantPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.entityId);
    }

    // データの読み込み
    public static SelectServantPacket decode(FriendlyByteBuf buf) {
        return new SelectServantPacket(buf.readUtf());
    }

    // サーバー側での処理
    public static void handle(SelectServantPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                ItemStack ring = player.getMainHandItem();
                if (ring.getItem() instanceof CalamityRingItem) {
                    // 指輪のNBTに「選択中のモブID」を書き込む
                    ring.getOrCreateTag().putString("SelectedMob", msg.entityId);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
