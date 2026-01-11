package saga.ticex_annihilation.registries;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import saga.ticex_annihilation.TimexAnnihilation;
import saga.ticex_annihilation.block.MinofskyDiffuserBlock;
import saga.ticex_annihilation.block.NeutronJammerBlock;

import java.util.function.Supplier;

public class BlockRegistry {
    // メインクラスと合わせるため「BLOCKS」にする
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, TimexAnnihilation.MODID);

    public static final RegistryObject<Block> MINOFSKY_DIFFUSER = registerBlock("minofsky_diffuser",
            () -> new MinofskyDiffuserBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(5.0f).noOcclusion()));

    public static final RegistryObject<Block> NEUTRON_JAMMER = registerBlock("neutron_jammer",
            () -> new NeutronJammerBlock(BlockBehaviour.Properties.copy(Blocks.NETHERITE_BLOCK).strength(15.0f).noOcclusion()));

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, RegistryObject<T> block) {
        // BlockItemの登録先（ItemRegistry.ITEMS）も確認してください
        ItemRegistry.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }
}