package saga.ticex_annihilation.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class MinofskyDrivePacket {
    public MinofskyDrivePacket() {}
    public MinofskyDrivePacket(FriendlyByteBuf buf) {}
    public void toBytes(FriendlyByteBuf buf) {}

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                // NBTのフラグを反転（トグル）
                boolean currentState = player.getPersistentData().getBoolean("MinofskyDriveActive");
                boolean newState = !currentState;
                player.getPersistentData().putBoolean("MinofskyDriveActive", newState);

                // プレイヤーに状態を通知
                String msg = newState ? "§b[Minofsky Drive] ACTIVE" : "§c[Minofsky Drive] INACTIVE";
                player.displayClientMessage(Component.literal(msg), true);
            }
        });
        return true;
    }
}
