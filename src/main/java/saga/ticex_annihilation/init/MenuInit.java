package saga.ticex_annihilation.init;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import saga.ticex_annihilation.TimexAnnihilation;
import saga.ticex_annihilation.inventory.SATCMenu;

public class MenuInit {
    // MOD_ID を MODID に修正
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, TimexAnnihilation.MODID);

    public static final RegistryObject<MenuType<SATCMenu>> SATC_MENU = MENUS.register(
            "satc_menu",
            () -> IForgeMenuType.create(SATCMenu::new)
    );
}