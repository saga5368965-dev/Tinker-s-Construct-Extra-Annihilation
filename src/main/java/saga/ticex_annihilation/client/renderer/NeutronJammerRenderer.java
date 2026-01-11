package saga.ticex_annihilation.client.renderer;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import org.jetbrains.annotations.NotNull;
import saga.ticex_annihilation.block.entity.NeutronJammerBlockEntity;

public class NeutronJammerRenderer implements BlockEntityRenderer<NeutronJammerBlockEntity> {
    public NeutronJammerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(@NotNull NeutronJammerBlockEntity blockEntity, float partialTick, com.mojang.blaze3d.vertex.@NotNull PoseStack poseStack, net.minecraft.client.renderer.@NotNull MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        // 同様に、独自のモデル描画処理を記述
    }
}
