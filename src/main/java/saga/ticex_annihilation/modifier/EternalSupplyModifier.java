package saga.ticex_annihilation.modifier;

import io.redspace.ironsspellbooks.api.events.SpellOnCastEvent;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.event.common.GunFireEvent;
import com.tacz.guns.item.ModernKineticGunItem;
import com.tacz.guns.util.AttachmentDataUtils;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import saga.ticex_annihilation.registries.ModifierRegistry;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EternalSupplyModifier extends Modifier {

    public EternalSupplyModifier() {
        super();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onSpellCast(SpellOnCastEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity != null && hasModifierStatic(entity)) {
            event.setManaCost(0);
            MagicData pmg = MagicData.getPlayerMagicData(entity);
            if (pmg != null && pmg.getPlayerCooldowns() != null) {
                pmg.getPlayerCooldowns().addCooldown(event.getSpellId(), 1);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onGunFire(GunFireEvent event) {
        if (event.getShooter() instanceof Player player && hasModifierStatic(player)) {
            ItemStack gunStack = event.getGunItemStack();
            if (gunStack.getItem() instanceof ModernKineticGunItem gunItem) {
                TimelessAPI.getCommonGunIndex(gunItem.getGunId(gunStack)).ifPresent(index -> {
                    int maxAmmo = AttachmentDataUtils.getAmmoCountWithAttachment(gunStack, index.getGunData());
                    gunItem.setCurrentAmmoCount(gunStack, maxAmmo);
                    gunItem.setBulletInBarrel(gunStack, true);
                });
            }
        }
    }

    public static boolean hasModifierStatic(LivingEntity entity) {
        // バニラのスロットをチェック
        for (ItemStack stack : entity.getAllSlots()) {
            if (checkStack(stack)) return true;
        }

        // Curiosのスロットをチェック
        LazyOptional<ICuriosItemHandler> curiosLazy = CuriosApi.getCuriosHelper().getCuriosHandler(entity);
        return curiosLazy.map(handler -> {
            for (ICurioStacksHandler stacksHandler : handler.getCurios().values()) {
                IDynamicStackHandler stackHandler = stacksHandler.getStacks();
                for (int i = 0; i < stackHandler.getSlots(); i++) {
                    if (checkStack(stackHandler.getStackInSlot(i))) return true;
                }
            }
            return false;
        }).orElse(false);
    }

    private static boolean checkStack(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.getItem() instanceof slimeknights.tconstruct.library.tools.item.IModifiableDisplay) {
            try {
                // ModifierRegistryからこのModifierを取得してレベル判定
                return ToolStack.from(stack).getModifierLevel(ModifierRegistry.ETERNAL_SUPPLY.get()) > 0;
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }
}