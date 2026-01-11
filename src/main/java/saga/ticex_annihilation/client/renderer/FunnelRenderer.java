package saga.ticex_annihilation.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.api.item.ISpellbook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import saga.ticex_annihilation.entity.FunnelEntity;

public class FunnelRenderer extends EntityRenderer<FunnelEntity> {

    public FunnelRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    // --- 【追加】カリングによる消失を防ぐための判定 ---
    @Override
    public boolean shouldRender(@NotNull FunnelEntity entity, @NotNull net.minecraft.client.renderer.culling.Frustum frustum, double x, double y, double z) {
        // 通常の判定に加え、エンティティが生きているなら強制的に描画候補に入れる
        return super.shouldRender(entity, frustum, x, y, z) || entity.isAlive();
    }

    @Override
    public void render(FunnelEntity entity, float entityYaw, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight) {
        ItemStack stack = entity.getStoredItem();
        if (stack.isEmpty()) return;

        poseStack.pushPose();

        // 浮遊モーション
        double bob = Math.sin((entity.tickCount + partialTicks) * 0.1) * 0.1;
        poseStack.translate(0, 0.5 + bob, 0);

        // エンティティの向きに同期
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - entityYaw));

        // アイテムごとの個別調整
        if (stack.getItem() instanceof ISpellbook) {
            poseStack.mulPose(Axis.XP.rotationDegrees(-30.0F));
            poseStack.scale(1.1F, 1.1F, 1.1F);
        } else {
            // 銃は正面を向くように調整が必要な場合が多いです
            poseStack.mulPose(Axis.ZP.rotationDegrees(10.0F));
            poseStack.scale(0.85F, 0.85F, 0.85F);
        }

        // --- 【重要修正】FIXED ではなく GROUND または THIRD_PERSON を使用 ---
        Minecraft.getInstance().getItemRenderer().renderStatic(
                stack,
                ItemDisplayContext.GROUND, // GROUNDが最も安定して浮かびます
                packedLight,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                buffer,
                entity.level(),
                entity.getId()
        );

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull FunnelEntity entity) {
        return new ResourceLocation("minecraft", "textures/atlas/blocks.png");
    }
}