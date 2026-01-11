package saga.ticex_annihilation.registries;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import saga.ticex_annihilation.TimexAnnihilation; // 綴りを修正
import saga.ticex_annihilation.entity.FunnelEntity;

public class EntityRegistry {
    // MODID に修正
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, TimexAnnihilation.MODID);

    public static final RegistryObject<EntityType<FunnelEntity>> FUNNEL = ENTITIES.register("funnel",
            () -> EntityType.Builder.<FunnelEntity>of(FunnelEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(10)
                    .build("funnel"));

    public static void register(IEventBus eventBus) {
        ENTITIES.register(eventBus);
    }
}