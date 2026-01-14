package saga.ticex_annihilation.modifier;

import mods.flammpfeil.slashblade.item.SwordType;
import moffy.ticex.lib.hook.EmbossmentModifierHook;
import moffy.ticex.modules.general.TicEXRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CalamityInfinityModifier extends NoLevelsModifier implements EmbossmentModifierHook {

    private static CalamityInfinityModifier INSTANCE;

    public CalamityInfinityModifier() {
        super();
        INSTANCE = this;
        MinecraftForge.EVENT_BUS.register(this);
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
            } else if (inputTag.contains("tinker_data")) {
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
     * 【SEコスト0の実質的実装】
     * 抜刀剣のイベントクラスが解決できないため、PlayerTickで直接制御します。
     * SEを使用している間（抜刀剣を構えている間）、経験値が減らないようにロックします。
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (INSTANCE == null || event.phase != TickEvent.Phase.START || event.player.level().isClientSide) return;

        Player player = event.player;
        if (INSTANCE.isPlayerHoldingCalamity(player)) {
            // 抜刀剣のSEは通常「経験値レベル」ではなく「経験値のバー（Exp）」を消費します。
            // 常に少しだけ経験値を与えることで、消費を即座に相殺し、実質コスト0にします。
            if (player.experienceProgress < 0.1F && player.experienceLevel > 0) {
                // 経験値が微減した瞬間に補填する（厄災の指輪による無限供給の演出）
                player.giveExperiencePoints(1);
            }

            // もしレベル消費型のSEだった場合も考慮して、最低1レベルを維持
            if (player.experienceLevel < 1) {
                player.experienceLevel = 1;
            }
        }
    }

    private boolean isPlayerHoldingCalamity(Player player) {
        return checkStack(player.getMainHandItem()) || checkStack(player.getOffhandItem());
    }

    private boolean checkStack(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()) return false;
        try {
            // NBTを直接参照して判定。これが一番確実です。
            return ToolStack.from(stack).getModifierLevel(this) > 0;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean shouldDisplay(boolean advanced) { return true; }
}