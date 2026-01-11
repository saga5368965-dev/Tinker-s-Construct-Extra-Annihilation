package saga.ticex_annihilation.client.renderer;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import saga.ticex_annihilation.block.entity.MinofskyDiffuserBlockEntity;

public class MinofskyDiffuserRenderer implements BlockEntityRenderer<MinofskyDiffuserBlockEntity> {
    public MinofskyDiffuserRenderer(BlockEntityRendererProvider.Context context) {
        // ここでモデルの初期化などを行いますが、まずは空でOK
    }

    @Override
    public void render(MinofskyDiffuserBlockEntity blockEntity, float partialTick, com.mojang.blaze3d.vertex.PoseStack poseStack, net.minecraft.client.renderer.MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        // 独自の3Dモデル（Geoモデルなど）を呼び出す処理をここに記述します
    }
}
