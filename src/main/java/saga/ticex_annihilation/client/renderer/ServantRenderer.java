package saga.ticex_annihilation.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.ForgeRegistries;
import saga.ticex_annihilation.entity.ServantEntity;

import java.util.HashMap;
import java.util.Map;

public class ServantRenderer extends EntityRenderer<ServantEntity> {
    // キャッシュ: 同じ種類のモブを何度も生成し直すと重いので一時保存
    private final Map<String, LivingEntity> renderCache = new HashMap<>();

    public ServantRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(ServantEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        String originalId = entity.getPersistentData().getString("OriginalID");
        if (originalId.isEmpty()) originalId = "minecraft:zombie";

        // キャッシュから偽装用モブを取得、なければ作成
        LivingEntity fakeEntity = renderCache.computeIfAbsent(originalId, id -> {
            EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(id));
            if (type != null) {
                Entity e = type.create(entity.level());
                return e instanceof LivingEntity ? (LivingEntity) e : null;
            }
            return null;
        });

        if (fakeEntity != null) {
            // 見た目の同期: ServantEntityの動きを偽装用モブにコピー
            copyEntityState(entity, fakeEntity, partialTicks);

            // 偽装用モブのレンダラーを呼び出して描画
            EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
            dispatcher.render(fakeEntity, 0, 0, 0, entityYaw, partialTicks, poseStack, buffer, packedLight);
        }

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private void copyEntityState(ServantEntity from, LivingEntity to, float partialTicks) {
        to.setPos(from.getX(), from.getY(), from.getZ());
        to.xo = from.xo; to.yo = from.yo; to.zo = from.zo;
        to.yRotO = from.yRotO;
        to.setYRot(from.getYRot());
        to.setXRot(from.getXRot());
        to.yBodyRot = from.yBodyRot;
        to.yHeadRot = from.yHeadRot;
        to.tickCount = from.tickCount;
        to.swingTime = from.swingTime;
        // しもべの状態（座っている、腕を上げている等）もここで同期可能
    }

    @Override
    public ResourceLocation getTextureLocation(ServantEntity entity) {
        // 実際には使用されない（偽装用モブのテクスチャが使われるため）
        return new ResourceLocation("minecraft", "textures/entity/zombie/zombie.png");
    }
}
