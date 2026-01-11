package saga.ticex_annihilation.modifier;

import io.redspace.ironsspellbooks.api.events.SpellOnCastEvent;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.network.ClientboundSyncCooldowns;
import io.redspace.ironsspellbooks.setup.Messages;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.event.common.GunFireEvent;
import com.tacz.guns.item.ModernKineticGunItem;
import com.tacz.guns.util.AttachmentDataUtils;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.common.TinkerTags;

import org.jetbrains.annotations.NotNull;
import java.util.HashMap;
import java.util.UUID;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class EternalSupplyModifier extends Modifier {

    private static final Set<UUID> ACTIVE_PLAYERS = ConcurrentHashMap.newKeySet();

    public EternalSupplyModifier() {
        super();
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    @NotNull
    public Component getDisplayName(int level) {
        return Component.translatable("modifier.ticex_annihilation.eternal_supply");
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;

        ServerPlayer player = (ServerPlayer) event.player;
        boolean hasModifier = false;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.is(TinkerTags.Items.MODIFIABLE)) {
                try {
                    ToolStack tool = ToolStack.from(stack);
                    if (tool.getModifierLevel(this) > 0) {
                        hasModifier = true;
                        break;
                    }
                } catch (Exception ignored) {}
            }
        }

        if (hasModifier) {
            ACTIVE_PLAYERS.add(player.getUUID());
            handleEternalResources(player);
        } else {
            ACTIVE_PLAYERS.remove(player.getUUID());
        }
    }

    private void handleEternalResources(ServerPlayer player) {
        // --- Iron's Spells: クールタイム解消とマナ満タン ---
        MagicData pmg = MagicData.getPlayerMagicData(player);
        if (pmg != null) {
            var cdManager = pmg.getPlayerCooldowns();
            if (cdManager != null) {
                SpellRegistry.REGISTRY.get().forEach(s -> cdManager.removeCooldown(s.getSpellId()));
                Messages.sendToPlayer(new ClientboundSyncCooldowns(new HashMap<>()), player);
            }
            // getManaCapacity() が使えない場合、非常に大きな値をセットするか
            // 内部的な属性値に頼らず直接現在のマナを固定します。
            pmg.setMana(10000.0F);
        }

        // --- TACZ: 弾薬とチャンバーの状態を強制固定 ---
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.getItem() instanceof ModernKineticGunItem gunItem) {
            // 最大弾数をアタッチメント込みで取得
            int maxAmmo = TimelessAPI.getCommonGunIndex(gunItem.getGunId(mainHand))
                    .map(index -> AttachmentDataUtils.getAmmoCountWithAttachment(mainHand, index.getGunData()))
                    .orElse(100); // 取得失敗時のデフォルト値

            gunItem.setCurrentAmmoCount(mainHand, maxAmmo);
            gunItem.setBulletInBarrel(mainHand, true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onGunFire(GunFireEvent event) {
        if (event.getShooter() instanceof ServerPlayer player && ACTIVE_PLAYERS.contains(player.getUUID())) {
            ItemStack gunStack = event.getGunItemStack();
            if (gunStack.getItem() instanceof ModernKineticGunItem gunItem) {
                int maxAmmo = TimelessAPI.getCommonGunIndex(gunItem.getGunId(gunStack))
                        .map(index -> AttachmentDataUtils.getAmmoCountWithAttachment(gunStack, index.getGunData()))
                        .orElse(100);
                gunItem.setCurrentAmmoCount(gunStack, maxAmmo);
                gunItem.setBulletInBarrel(gunStack, true);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onSpellCast(SpellOnCastEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && ACTIVE_PLAYERS.contains(player.getUUID())) {
            event.setManaCost(0);
        }
    }
}