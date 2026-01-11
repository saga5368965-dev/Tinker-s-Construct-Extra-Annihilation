package saga.ticex_annihilation.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import saga.ticex_annihilation.TimexAnnihilation;
import saga.ticex_annihilation.registries.BlockEntityRegistry;
import saga.ticex_annihilation.client.renderer.MinofskyDiffuserRenderer;
import saga.ticex_annihilation.client.renderer.NeutronJammerRenderer;

@Mod.EventBusSubscriber(modid = TimexAnnihilation.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // ブロックエンティティに専用のレンダラー（モデル描画）を紐付け
        event.registerBlockEntityRenderer(BlockEntityRegistry.MINOFSKY_DIFFUSER.get(),
                MinofskyDiffuserRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityRegistry.NEUTRON_JAMMER.get(),
                NeutronJammerRenderer::new);
    }
}
