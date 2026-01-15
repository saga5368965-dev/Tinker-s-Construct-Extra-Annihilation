package saga.ticex_annihilation.modifier;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.ModList;

import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import saga.ticex_annihilation.registries.ModifierRegistry;
import moffy.ticex.item.modifiable.ModifiableIronsSpellbookItem;

import java.util.UUID;

/**
 * [高速詠唱 / Instant Cast]
 * 形式: Forge Event方式 (EternalSupplyModifier準拠)
 * 効果: ISSの詠唱時間を属性操作によって実質0にする。
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class InstantCastModifier extends Modifier {

    private static final UUID INSTANT_CAST_UUID = UUID.fromString("c226e9b2-326b-4e1c-b5f7-879685e92134");

    public InstantCastModifier() {
        super();
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        // ISSが未導入、またはクライアント側ならスキップ
        if (!ModList.get().isLoaded("irons_spellbooks") ||
                event.phase != TickEvent.Phase.START ||
                event.player.level().isClientSide) return;

        Player player = event.player;
        AttributeInstance castTimeAttr = player.getAttribute(AttributeRegistry.CAST_TIME_REDUCTION.get());

        if (castTimeAttr == null) return;

        // 手持ちまたはCuriosに「InstantCast付き魔導書」があるか
        if (isEquippedOrHeld(player, ModifiableIronsSpellbookItem.class)) {
            if (castTimeAttr.getModifier(INSTANT_CAST_UUID) == null) {
                // 10.0 (1000%短縮) を加算
                castTimeAttr.addTransientModifier(new AttributeModifier(INSTANT_CAST_UUID, "Annihilation Instant Cast", 10.0, AttributeModifier.Operation.ADDITION));
            }
        } else {
            // 条件を満たさなくなったら属性を削除
            if (castTimeAttr.getModifier(INSTANT_CAST_UUID) != null) {
                castTimeAttr.removeModifier(INSTANT_CAST_UUID);
            }
        }
    }

    private static boolean isEquippedOrHeld(Player player, Class<?> itemClass) {
        // 1. 手持ちチェック
        if (checkWithModifier(player.getMainHandItem(), itemClass)) return true;
        if (checkWithModifier(player.getOffhandItem(), itemClass)) return true;

        // 2. Curiosチェック
        LazyOptional<ICuriosItemHandler> curiosOpt = CuriosApi.getCuriosHelper().getCuriosHandler(player);
        if (curiosOpt.isPresent()) {
            ICuriosItemHandler handler = curiosOpt.orElseThrow(IllegalStateException::new);
            for (ICurioStacksHandler stacksHandler : handler.getCurios().values()) {
                IDynamicStackHandler stackHandler = stacksHandler.getStacks();
                for (int i = 0; i < stackHandler.getSlots(); i++) {
                    ItemStack stack = stackHandler.getStackInSlot(i);
                    if (checkWithModifier(stack, itemClass)) return true;
                }
            }
        }
        return false;
    }

    private static boolean checkWithModifier(ItemStack stack, Class<?> itemClass) {
        if (!stack.isEmpty() && itemClass.isInstance(stack.getItem())) {
            try {
                if (stack.getItem() instanceof slimeknights.tconstruct.library.tools.item.IModifiableDisplay) {
                    // ModifierRegistryのINSTANT_CASTがこのアイテムについているか判定
                    return ToolStack.from(stack).getModifierLevel(ModifierRegistry.INSTANT_CAST.get()) > 0;
                }
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }
}