package saga.ticex_annihilation.registries.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import saga.ticex_annihilation.item.CalamityRingItem;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.function.Supplier;

public class OpenCalamityGuiPacket {
    public OpenCalamityGuiPacket() {}
    public static void encode(OpenCalamityGuiPacket msg, FriendlyByteBuf buf) {}
    public static OpenCalamityGuiPacket decode(FriendlyByteBuf buf) { return new OpenCalamityGuiPacket(); }

    public static void handle(OpenCalamityGuiPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                // Curios枠から指輪を探し、あれば機能(onFunctionKeyPressed)を実行
                CuriosApi.getCuriosHelper().findFirstCurio(player, stack -> stack.getItem() instanceof CalamityRingItem)
                        .ifPresent(result -> {
                            ((CalamityRingItem)result.stack().getItem()).onFunctionKeyPressed(player, result.stack());
                        });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}