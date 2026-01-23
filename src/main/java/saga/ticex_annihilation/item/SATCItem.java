package saga.ticex_annihilation.item;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import saga.ticex_annihilation.entity.FunnelEntity;
import saga.ticex_annihilation.inventory.SATCMenu;
import saga.ticex_annihilation.registries.EntityRegistry;
import saga.ticex_annihilation.registries.ModifierRegistry;
import slimeknights.tconstruct.library.tools.definition.ToolDefinition;
import slimeknights.tconstruct.library.tools.item.IModifiable;
import slimeknights.tconstruct.library.tools.item.ModifiableItem;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

import java.util.Optional;

public class SATCItem extends ModifiableItem {

    public static final ResourceLocation ACTIVE_KEY = new ResourceLocation("ticex_annihilation", "satc_active");
    public static final ResourceLocation MODE_KEY = new ResourceLocation("ticex_annihilation", "satc_mode");

    public SATCItem(Properties properties, ToolDefinition toolDefinition) {
        super(properties, toolDefinition);
    }

    // --- 静的ユーティリティ (パケット解決用) ---

    /**
     * パケットクラスから呼び出される、現在有効なSATCを探すメソッド
     */
    public static Optional<ItemStack> findActiveSATC(Player player) {
        if (player.getMainHandItem().getItem() instanceof SATCItem) return Optional.of(player.getMainHandItem());
        if (player.getOffhandItem().getItem() instanceof SATCItem) return Optional.of(player.getOffhandItem());

        return CuriosApi.getCuriosHelper().findFirstCurio(player, s -> s.getItem() instanceof SATCItem)
                .map(slotResult -> slotResult.stack());
    }

    // --- 無限補給システム ---

    private void supplyInternalWeapons(ItemStack satcStack) {
        if (!(satcStack.getItem() instanceof IModifiable)) return;

        ToolStack satcTool = ToolStack.from(satcStack);
        boolean hasEternalSupply = satcTool.getModifierLevel(ModifierRegistry.ETERNAL_SUPPLY.get()) > 0;

        if (!hasEternalSupply) return;

        CompoundTag nbt = satcStack.getOrCreateTag();
        if (!nbt.contains("Inventory")) return;

        ItemStackHandler handler = new ItemStackHandler(18);
        handler.deserializeNBT(nbt.getCompound("Inventory"));

        boolean changed = false;
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack weapon = handler.getStackInSlot(i);
            if (!weapon.isEmpty()) {
                if (weapon.isDamaged()) {
                    weapon.setDamageValue(0);
                    changed = true;
                }
            }
        }

        if (changed) {
            nbt.put("Inventory", handler.serializeNBT());
        }
    }

    // --- Tick 処理 ---

    @Override
    public void onInventoryTick(ItemStack stack, Level level, Player player, int slotIndex, int selectedIndex) {
        super.onInventoryTick(stack, level, player, slotIndex, selectedIndex);
        if (!level.isClientSide && player.tickCount % 20 == 0) {
            supplyInternalWeapons(stack);
        }
    }

    @Override
    public @Nullable ICapabilityProvider initCapabilities(@NotNull ItemStack stack, @Nullable CompoundTag nbt) {
        return CuriosApi.createCurioProvider(new ICurio() {
            @Override public ItemStack getStack() { return stack; }
            @Override
            public void curioTick(SlotContext slotContext) {
                if (!slotContext.entity().level().isClientSide && slotContext.entity().tickCount % 20 == 0) {
                    supplyInternalWeapons(stack);
                }
            }
            @Override
            public void onUnequip(SlotContext slotContext, ItemStack newStack) {
                if (slotContext.entity() instanceof Player player) recallAllFunnels(player);
            }
        });
    }

    // --- 制御ロジック (パケット経由で実行) ---

    public void toggleFunnels(ItemStack stack, Player player) {
        ToolStack tool = ToolStack.from(stack);
        boolean isActive = tool.getPersistentData().getBoolean(ACTIVE_KEY);

        if (!isActive) {
            if (isInventoryEmpty(stack)) return;
            deployFunnels(stack, player);
            tool.getPersistentData().putBoolean(ACTIVE_KEY, true);
            player.displayClientMessage(Component.translatable("message.ticex_annihilation.deploy").withStyle(ChatFormatting.AQUA), true);
        } else {
            recallAllFunnels(player);
            tool.getPersistentData().putBoolean(ACTIVE_KEY, false);
            player.displayClientMessage(Component.translatable("message.ticex_annihilation.recall").withStyle(ChatFormatting.GRAY), true);
        }
    }

    public void cycleMode(ItemStack stack, Player player) {
        ToolStack tool = ToolStack.from(stack);
        int currentMode = tool.getPersistentData().getInt(MODE_KEY);
        int nextMode = (currentMode + 1) % 3; // 0, 1, 2 の3モード想定
        tool.getPersistentData().putInt(MODE_KEY, nextMode);

        player.displayClientMessage(
                Component.translatable("message.ticex_annihilation.mode." + nextMode).withStyle(ChatFormatting.GOLD),
                true
        );
    }

    // --- 内部処理 ---

    private void deployFunnels(ItemStack stack, Player player) {
        CompoundTag nbt = stack.getOrCreateTag();
        if (!nbt.contains("Inventory")) return;

        ItemStackHandler handler = new ItemStackHandler(18);
        handler.deserializeNBT(nbt.getCompound("Inventory"));

        for (int i = 0; i < 18; i++) {
            ItemStack weapon = handler.getStackInSlot(i);
            if (weapon.isEmpty()) continue;

            FunnelEntity funnel = EntityRegistry.FUNNEL.get().create(player.level());
            if (funnel != null) {
                funnel.setOwner(player);
                funnel.setFunnelIndex(i);
                funnel.setStoredItem(weapon.copy());
                funnel.setPos(player.getX(), player.getEyeY(), player.getZ());
                player.level().addFreshEntity(funnel);
            }
        }
    }

    public void recallAllFunnels(Player player) {
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.getEntities().getAll().forEach(e -> {
                if (e instanceof FunnelEntity f && player.getUUID().equals(f.getOwner() != null ? f.getOwner().getUUID() : null)) {
                    f.discard();
                }
            });
        }
    }

    private boolean isInventoryEmpty(ItemStack stack) {
        CompoundTag nbt = stack.getTag();
        if (nbt == null || !nbt.contains("Inventory")) return true;
        ItemStackHandler handler = new ItemStackHandler(18);
        handler.deserializeNBT(nbt.getCompound("Inventory"));
        for (int i = 0; i < 18; i++) {
            if (!handler.getStackInSlot(i).isEmpty()) return false;
        }
        return true;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) return InteractionResultHolder.success(stack);
        if (player.isShiftKeyDown()) {
            NetworkHooks.openScreen((ServerPlayer) player, new SimpleMenuProvider(
                    (id, inv, p) -> new SATCMenu(id, inv, stack),
                    Component.translatable("gui.ticex_annihilation.satc_title")
            ), buf -> buf.writeItem(stack));
        } else {
            toggleFunnels(stack, player);
        }
        return InteractionResultHolder.consume(stack);
    }

    @Override public @NotNull Rarity getRarity(@NotNull ItemStack stack) { return Rarity.EPIC; }
}