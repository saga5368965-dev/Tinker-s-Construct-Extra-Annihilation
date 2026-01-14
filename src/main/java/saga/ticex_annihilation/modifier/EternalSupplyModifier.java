package saga.ticex_annihilation.modifier;

import io.redspace.ironsspellbooks.api.events.SpellOnCastEvent;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.event.common.GunFireEvent;
import com.tacz.guns.item.ModernKineticGunItem;
import com.tacz.guns.util.AttachmentDataUtils;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.item.IModifiableDisplay;
import slimeknights.tconstruct.common.TinkerTags;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

// @Mod.EventBusSubscriber を使うことで、Modifierのインスタンス生成とは無関係にイベントを確実に拾います
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EternalSupplyModifier extends Modifier {

    // 静的判定のためにModifierの参照を保持（Registryから取得する形でも可）
    private static EternalSupplyModifier INSTANCE;

    public EternalSupplyModifier() {
        super();
        INSTANCE = this;
    }

    @Override
    @NotNull
    public Component getDisplayName(int level) {
        return Component.translatable("modifier.ticex_annihilation.eternal_supply");
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onSpellCast(SpellOnCastEvent event) {
        if (INSTANCE == null) return;
        Player player = event.getEntity();
        if (player instanceof ServerPlayer serverPlayer && INSTANCE.hasModifier(serverPlayer)) {
            event.setManaCost(0);
            MagicData pmg = MagicData.getPlayerMagicData(serverPlayer);
            if (pmg != null && pmg.getPlayerCooldowns() != null) {
                pmg.getPlayerCooldowns().addCooldown(event.getSpellId(), 2);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onGunFire(GunFireEvent event) {
        if (INSTANCE == null) return;
        if (event.getShooter() instanceof Player player && INSTANCE.hasModifier(player)) {
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

    private boolean hasModifier(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (checkStack(stack)) return true;
        }
        for (ItemStack stack : player.getInventory().armor) {
            if (checkStack(stack)) return true;
        }
        return checkStack(player.getOffhandItem());
    }

    private boolean checkStack(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.getItem() instanceof IModifiableDisplay || stack.is(TinkerTags.Items.MODIFIABLE)) {
            try {
                if (stack.hasTag() && Objects.requireNonNull(stack.getTag()).contains("tinker_data")) {
                    return ToolStack.from(stack).getModifierLevel(this) > 0;
                }
            } catch (Exception ignored) {}
        }
        return false;
    }
}