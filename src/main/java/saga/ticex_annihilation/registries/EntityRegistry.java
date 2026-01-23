package saga.ticex_annihilation.registries;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import saga.ticex_annihilation.TimexAnnihilation;
import saga.ticex_annihilation.entity.FunnelEntity;
import saga.ticex_annihilation.entity.ServantEntity; // しもべエンティティをインポート

public class EntityRegistry {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, TimexAnnihilation.MODID);

    // ファンネルの登録
    public static final RegistryObject<EntityType<FunnelEntity>> FUNNEL = ENTITIES.register("funnel",
            () -> EntityType.Builder.<FunnelEntity>of(FunnelEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(10)
                    .build("funnel"));

    // ★ しもべ（軍勢）の登録
    public static final RegistryObject<EntityType<ServantEntity>> SERVANT_ENTITY = ENTITIES.register("servant_entity",
            () -> EntityType.Builder.<ServantEntity>of(ServantEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.95F) // 標準的な人型サイズ
                    .clientTrackingRange(10)
                    .build("servant_entity"));

    public static void register(IEventBus eventBus) {
        ENTITIES.register(eventBus);
    }
}