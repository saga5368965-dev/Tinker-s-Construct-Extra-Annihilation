package saga.ticex_annihilation.modifier;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import mods.flammpfeil.slashblade.item.SwordType;
import moffy.ticex.lib.hook.EmbossmentModifierHook;
import moffy.ticex.modules.general.TicEXRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap.Builder;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import java.util.EnumSet;
import java.util.UUID;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CalamityInfinityModifier extends NoLevelsModifier implements EmbossmentModifierHook {

    private static CalamityInfinityModifier INSTANCE;
    // 属性操作用のユニークID
    private static final UUID COOLDOWN_MODIFIER_UUID = UUID.fromString("f426e9b2-326b-4e1c-b5f7-879685e92134");

    public CalamityInfinityModifier() {
        super();
        INSTANCE = this;
    }

    @Override
    protected void registerHooks(Builder hookBuilder) {
        hookBuilder.addHook(this, TicEXRegistry.EMBOSSMENT_HOOK);
    }

    @Override
    public boolean applyItem(EmbossmentContext context, int inputIndex, boolean secondary) {
        ItemStack input = context.getInputStack(inputIndex);
        ItemStack toolStack = context.getToolStack();
        EnumSet<SwordType> swordTypes = SwordType.from(toolStack);

        if (swordTypes.contains(SwordType.BEWITCHED)) {
            CompoundTag inputTag = input.getOrCreateTag();
            CompoundTag bladeStateTag = null;

            if (inputTag.contains("bladeState")) {
                bladeStateTag = inputTag.getCompound("bladeState");
            }

            if (bladeStateTag != null) {
                toolStack.getOrCreateTag().put("bladeState", bladeStateTag.copy());
                return true;
            } else {
                context.setErrorMsg(Component.literal("§c[Calamity] No BladeState found."));
                return false;
            }
        }
        return false;
    }

    /**
     * 属性操作とSEコスト維持
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (INSTANCE == null || event.phase != TickEvent.Phase.START || event.player.level().isClientSide) return;

        Player player = event.player;
        AttributeInstance cooldownAttr = player.getAttribute(AttributeRegistry.COOLDOWN_REDUCTION.get());

        if (INSTANCE.isPlayerHoldingCalamity(player)) {
            // --- 属性操作: クールタイム短縮を+1000% (実質10倍速＝ほぼゼロ) ---
            if (cooldownAttr != null && cooldownAttr.getModifier(COOLDOWN_MODIFIER_UUID) == null) {
                cooldownAttr.addTransientModifier(new AttributeModifier(COOLDOWN_MODIFIER_UUID, "Calamity Infinity Cooldown", 10.0, AttributeModifier.Operation.ADDITION));
            }

            // --- SEコスト補填 ---
            if (player.experienceProgress < 0.1F && player.experienceLevel > 0) {
                player.giveExperiencePoints(1);
            }
            if (player.experienceLevel < 1) {
                player.experienceLevel = 1;
            }
        } else {
            // --- Modifierを持っていない時は属性を即座に除去 ---
            if (cooldownAttr != null && cooldownAttr.getModifier(COOLDOWN_MODIFIER_UUID) != null) {
                cooldownAttr.removeModifier(COOLDOWN_MODIFIER_UUID);
            }
        }
    }

    private boolean isPlayerHoldingCalamity(Player player) {
        return checkStack(player.getMainHandItem()) || checkStack(player.getOffhandItem());
    }

    private boolean checkStack(ItemStack stack) {
        if (stack.isEmpty()) return false;
        try {
            // TiC公式の判定に修正
            return ToolStack.from(stack).getModifierLevel(this) > 0;
        } catch (Exception e) {
            return false;
        }
    }
}