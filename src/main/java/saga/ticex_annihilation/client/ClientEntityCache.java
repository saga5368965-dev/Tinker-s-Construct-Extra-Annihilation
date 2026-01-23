package saga.ticex_annihilation.client;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

public class ClientEntityCache {
    private static final Map<String, LivingEntity> CACHE = new HashMap<>();

    public static LivingEntity getOrCreate(String entityId) {
        return CACHE.computeIfAbsent(entityId, id -> {
            EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(id));
            if (type != null) {
                Entity entity = type.create(Minecraft.getInstance().level);
                return entity instanceof LivingEntity le ? le : null;
            }
            return null;
        });
    }
}
