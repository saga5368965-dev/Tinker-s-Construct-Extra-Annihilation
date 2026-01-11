package saga.ticex_annihilation.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import java.util.Collections;
import java.util.List;

public class NeutronJammerEffect extends MobEffect {
    public NeutronJammerEffect(MobEffectCategory category, int color) {
        super(category, color);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        return Collections.emptyList(); // 絶対解除不能
    }

    @SubscribeEvent
    public void onNuclearExplosion(ExplosionEvent.Start event) {
        if (isJammingActive(event)) {
            // 爆発源のエンティティ、または爆発自体の判定
            var exploder = event.getExplosion().getExploder();
            String name = exploder != null ? exploder.getType().getDescriptionId().toLowerCase() : "";

            // 1. キーワード判定（核、水爆、原子炉、アトミック）
            boolean isNuclear = name.contains("nuclear") || name.contains("h_bomb") ||
                    name.contains("hydrogen") || name.contains("reactor") ||
                    name.contains("atomic");

            // 2. アイテムタグ判定 (爆発源がアイテム、または特定のタグを持つエンティティの場合)
            // 例: forge:nukes や forge:bombs/nuclear などを想定
            if (!isNuclear && exploder instanceof LivingEntity living) {
                ItemStack held = living.getMainHandItem();
                isNuclear = held.getTags().anyMatch(tag -> tag.location().getPath().contains("nuke") ||
                        tag.location().getPath().contains("nuclear"));
            }

            if (isNuclear) {
                event.setCanceled(true); // 物理的に不発化
                // 対策（NJC）されたらこれは実行されない＝NJC搭載機とみなす
            }
        }
    }

    private boolean isJammingActive(ExplosionEvent.Start event) {
        Vec3 pos = event.getExplosion().getPosition();
        // toAABBの代わりに、中心点から128ブロック広げたAABBを手動で作成
        AABB searchArea = new AABB(pos.x - 128, pos.y - 128, pos.z - 128,
                pos.x + 128, pos.y + 128, pos.z + 128);

        // getEntitiesOfClassの結果をLivingEntityとして明示的に扱う
        List<LivingEntity> entities = event.getLevel().getEntitiesOfClass(LivingEntity.class, searchArea);

        for (LivingEntity entity : entities) {
            // hasEffectを確実に呼び出す
            if (entity.hasEffect(this)) {
                return true;
            }
        }
        return false;
    }
}