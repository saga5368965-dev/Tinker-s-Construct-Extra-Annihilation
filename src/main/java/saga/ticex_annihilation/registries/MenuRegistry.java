package saga.ticex_annihilation.registries;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import saga.ticex_annihilation.TimexAnnihilation;
import saga.ticex_annihilation.inventory.CalamityArmyMenu;
import saga.ticex_annihilation.inventory.CalamityRingMenu;

public class MenuRegistry {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, TimexAnnihilation.MODID);

    // 飢餓用 (1000スロット)
    public static final RegistryObject<MenuType<CalamityRingMenu>> CALAMITY_RING_MENU = MENUS.register("calamity_ring_menu",
            () -> IForgeMenuType.create((windowId, inv, data) -> new CalamityRingMenu(windowId, inv, data.readItem())));

    // 支配用 (Morph風・軍勢装備)
    public static final RegistryObject<MenuType<CalamityArmyMenu>> CALAMITY_ARMY_MENU = MENUS.register("calamity_army_menu",
            () -> IForgeMenuType.create((windowId, inv, data) -> new CalamityArmyMenu(windowId, inv, data.readItem())));
}
