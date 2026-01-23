package saga.ticex_annihilation.modifier;

import moffy.ticex.lib.hook.EmbossmentModifierHook;
import moffy.ticex.modules.general.TicEXRegistry;
import mods.flammpfeil.slashblade.entity.BladeStandEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap.Builder;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CalamityInfinityModifier extends NoLevelsModifier implements EmbossmentModifierHook {

    private static CalamityInfinityModifier INSTANCE;

    public CalamityInfinityModifier() {
        super();
        INSTANCE = this;
    }

    @Override
    public void registerHooks(Builder hookBuilder) {
        hookBuilder.addHook(this, TicEXRegistry.EMBOSSMENT_HOOK);
    }

    // --- メソッド1: TicEX 金床合成時のフック (捕食システム) ---
    @Override
    public boolean applyItem(EmbossmentContext context, int inputIndex, boolean secondary) {
        ItemStack toolStack = context.getToolStack();
        if (toolStack.isEmpty()) return false;

        CompoundTag rootTag = toolStack.getOrCreateTag();
        ItemStack catalyst = ItemStack.EMPTY;
        boolean hasSoulSand = false;

        for (int i = 0; i < 6; i++) {
            ItemStack stack = context.getInputStack(i);
            if (stack == null || stack.isEmpty()) continue;

            ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
            if (id != null && id.toString().equals("minecraft:soul_sand")) {
                hasSoulSand = true;
            }

            if (stack.hasTag() && stack.getOrCreateTag().contains("embossed")) {
                catalyst = stack;
            }
        }

        if (hasSoulSand && !catalyst.isEmpty()) {
            return executePredation(rootTag, catalyst);
        }
        return false;
    }

    // --- メソッド2: 刀掛台への干渉 (魂の移植) ---
    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (INSTANCE == null) return;

        Level level = event.getLevel();
        Player player = event.getEntity();
        ItemStack catalyst = event.getItemStack();

        if (catalyst.isEmpty() || !catalyst.hasTag() || !catalyst.getOrCreateTag().contains("embossed")) return;

        if (event.getTarget() instanceof BladeStandEntity stand) {
            ItemStack sword = stand.getItem();
            if (sword.isEmpty()) return;

            if (INSTANCE.checkStack(sword)) {
                if (executePredation(sword.getOrCreateTag(), catalyst)) {
                    if (!level.isClientSide) {
                        level.playSound(null, stand.blockPosition(), SoundEvents.WITHER_SPAWN, SoundSource.BLOCKS, 0.7F, 2.0F);
                        ((ServerLevel)level).sendParticles(ParticleTypes.SOUL,
                                stand.getX(), stand.getY() + 0.5, stand.getZ(),
                                25, 0.1, 0.2, 0.1, 0.05);

                        if (!player.getAbilities().instabuild) catalyst.shrink(1);
                        player.displayClientMessage(Component.translatable("message.ticex_annihilation.predation_success")
                                .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), true);
                    }
                    event.setCanceled(true);
                    event.setCancellationResult(InteractionResult.SUCCESS);
                }
            }
        }
    }

    private static boolean executePredation(CompoundTag rootTag, ItemStack catalyst) {
        CompoundTag tag = catalyst.getTag();
        if (tag == null) return false;

        CompoundTag emb = tag.getCompound("embossed");
        CompoundTag state = null;

        if (emb.contains("tag") && emb.getCompound("tag").contains("bladeState")) {
            state = emb.getCompound("tag").getCompound("bladeState");
        } else if (emb.contains("bladeState")) {
            state = emb.getCompound("bladeState");
        }

        if (state != null) {
            if (!rootTag.contains("tconstruct:persistent_data")) {
                rootTag.put("tconstruct:persistent_data", new CompoundTag());
            }
            rootTag.getCompound("tconstruct:persistent_data").put("slashblade:blade_state", state.copy());
            rootTag.put("bladeState", state.copy());
            return true;
        }
        return false;
    }
    private boolean checkStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        try {
            return ToolStack.from(stack).getModifierLevel(this) > 0;
        } catch (Exception e) {
            return false;
        }
    }
}