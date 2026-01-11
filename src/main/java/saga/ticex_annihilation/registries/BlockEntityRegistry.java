package saga.ticex_annihilation.registries;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import saga.ticex_annihilation.TimexAnnihilation;
import saga.ticex_annihilation.block.entity.MinofskyDiffuserBlockEntity;
import saga.ticex_annihilation.block.entity.NeutronJammerBlockEntity;

public class BlockEntityRegistry {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, TimexAnnihilation.MODID);

    // ミノフスキー・ディフューザー
    public static final RegistryObject<BlockEntityType<MinofskyDiffuserBlockEntity>> MINOFSKY_DIFFUSER =
            BLOCK_ENTITIES.register("minofsky_diffuser",
                    () -> BlockEntityType.Builder.of(MinofskyDiffuserBlockEntity::new,
                            BlockRegistry.MINOFSKY_DIFFUSER.get()).build(null));

    // ニュートロンジャマー
    public static final RegistryObject<BlockEntityType<NeutronJammerBlockEntity>> NEUTRON_JAMMER =
            BLOCK_ENTITIES.register("neutron_jammer",
                    () -> BlockEntityType.Builder.of(NeutronJammerBlockEntity::new,
                            BlockRegistry.NEUTRON_JAMMER.get()).build(null));
}