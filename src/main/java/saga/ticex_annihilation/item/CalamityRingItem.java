package saga.ticex_annihilation.item;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import saga.ticex_annihilation.entity.ServantEntity;
import saga.ticex_annihilation.event.WarEventHandler;
import saga.ticex_annihilation.inventory.CalamityArmyMenu;
import saga.ticex_annihilation.inventory.CalamityRingInventory;
import saga.ticex_annihilation.inventory.CalamityRingMenu;
import saga.ticex_annihilation.registries.EntityRegistry;

import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;

public class CalamityRingItem extends Item implements MenuProvider {
    public static final int MODE_FAMINE = 0;   // 飢餓：装備集約
    public static final int MODE_CONQUEST = 1; // 支配：軍団使役
    public static final int MODE_WAR = 2;      // 戦争：戦域加速
    public static final int MODE_DEATH = 3;    // 死：終焉追撃

    private static final UUID FAMINE_ATTACK_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID FAMINE_ARMOR_UUID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    public CalamityRingItem(Properties props) {
        super(props.stacksTo(1).fireResistant());
    }

    public int getMode(ItemStack stack) {
        return stack.getOrCreateTag().getInt("Mode");
    }

    public void onFunctionKeyPressed(Player player, ItemStack stack) {
        if (player.level().isClientSide) return;
        switch (getMode(stack)) {
            case MODE_FAMINE -> NetworkHooks.openScreen((ServerPlayer) player, this, buf -> buf.writeItem(stack));
            case MODE_CONQUEST -> {
                if (player.isShiftKeyDown()) {
                    NetworkHooks.openScreen((ServerPlayer) player, new MenuProvider() {
                        @Override public @NotNull Component getDisplayName() { return Component.translatable("gui.ticex_annihilation.army.title"); }
                        @Override public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player p) {
                            return new CalamityArmyMenu(id, inv, stack);
                        }
                    }, buf -> buf.writeItem(stack));
                } else {
                    toggleArmy(player, stack);
                }
            }
            case MODE_WAR -> triggerWarFrenzy(player, stack);
            case MODE_DEATH -> activateDeathAura(player, stack);
        }
    }

    public void updateAttributeCache(ItemStack ring) {
        CalamityRingInventory inv = new CalamityRingInventory(ring);
        double totalAttack = 0;
        double totalArmor = 0;
        for (int i = 0; i < inv.getSlots(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (!stack.isEmpty()) {
                var attackMods = stack.getAttributeModifiers(EquipmentSlot.MAINHAND).get(Attributes.ATTACK_DAMAGE);
                for (var mod : attackMods) totalAttack += Objects.requireNonNull(mod).getAmount();
                var armorMods = stack.getAttributeModifiers(EquipmentSlot.CHEST).get(Attributes.ARMOR);
                for (var mod : armorMods) totalArmor += Objects.requireNonNull(mod).getAmount();
            }
        }
        CompoundTag nbt = ring.getOrCreateTag();
        nbt.putDouble("CachedAttack", totalAttack);
        nbt.putDouble("CachedArmor", totalArmor);
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slot, boolean selected) {
        if (level.isClientSide || !(entity instanceof Player player)) return;
        int mode = getMode(stack);
        if (mode == MODE_FAMINE) {
            applyFamineBuffs(player, stack);
        } else {
            removeFamineBuffs(player);
        }
        if (mode == MODE_DEATH) handleDeathHunt(player, stack);
    }

    private void applyFamineBuffs(Player player, ItemStack ring) {
        CompoundTag nbt = ring.getOrCreateTag();
        double bonusAttack = nbt.getDouble("CachedAttack");
        double bonusArmor = nbt.getDouble("CachedArmor");
        var attackAttr = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackAttr != null && attackAttr.getModifier(FAMINE_ATTACK_UUID) == null) {
            attackAttr.addTransientModifier(new AttributeModifier(FAMINE_ATTACK_UUID, "Famine Attack", bonusAttack, AttributeModifier.Operation.ADDITION));
        }
        var armorAttr = player.getAttribute(Attributes.ARMOR);
        if (armorAttr != null && armorAttr.getModifier(FAMINE_ARMOR_UUID) == null) {
            armorAttr.addTransientModifier(new AttributeModifier(FAMINE_ARMOR_UUID, "Famine Armor", bonusArmor, AttributeModifier.Operation.ADDITION));
        }
    }

    private void removeFamineBuffs(Player player) {
        Objects.requireNonNull(player.getAttribute(Attributes.ATTACK_DAMAGE)).removeModifier(FAMINE_ATTACK_UUID);
        Objects.requireNonNull(player.getAttribute(Attributes.ARMOR)).removeModifier(FAMINE_ARMOR_UUID);
    }

    // --- エラー修正：handleDeathHunt メソッドを追加 ---
    private void handleDeathHunt(Player player, ItemStack ring) {
        CompoundTag nbt = ring.getOrCreateTag();
        int ticks = nbt.getInt("DeathAuraTicks");
        if (ticks <= 0) return;

        CalamityRingInventory inv = new CalamityRingInventory(ring);
        if (inv.getStackInSlot(0).isEmpty()) {
            nbt.putInt("DeathAuraTicks", 0);
            player.displayClientMessage(Component.translatable("message.ticex_annihilation.death_fail").withStyle(ChatFormatting.RED), true);
            player.playSound(SoundEvents.VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(500), e -> e != player && e.isAlive())
                .stream().min(Comparator.comparingDouble(e -> e.distanceToSqr(player)))
                .ifPresent(target -> {
                    float damage = getConquestWeaponDamage(ring);
                    // WarEventHandler への呼び出し
                    WarEventHandler.performDeathWave(player.level(), (LivingEntity) player, target.position(), 8.0f, damage);
                    nbt.putInt("DeathAuraTicks", ticks - 1);
                });
    }

    public void onEntityKilled(Player killer, LivingEntity victim, ItemStack ring) {
        if (killer.level().isClientSide) return;
        float damage = getConquestWeaponDamage(ring);
        WarEventHandler.performDeathWave(killer.level(), (LivingEntity) killer, victim.position(), 8.0f, damage);
        absorbMob(ring, killer, victim);
        ring.getOrCreateTag().putInt("DeathEssence", ring.getOrCreateTag().getInt("DeathEssence") + 1);
    }

    private float getConquestWeaponDamage(ItemStack ring) {
        CalamityRingInventory inv = new CalamityRingInventory(ring);
        ItemStack weapon = inv.getStackInSlot(0);
        if (weapon.isEmpty()) return 10.0f;
        double baseDamage = 1.0;
        var modifiers = weapon.getAttributeModifiers(EquipmentSlot.MAINHAND).get(Attributes.ATTACK_DAMAGE);
        for (var modifier : modifiers) baseDamage += Objects.requireNonNull(modifier).getAmount();
        return (float) baseDamage + 10.0f;
    }

    private void toggleArmy(Player player, ItemStack stack) {
        Level level = player.level();
        CompoundTag nbt = stack.getOrCreateTag();
        String selectedId = nbt.getString("SelectedServantId");
        if (selectedId.isEmpty()) {
            ListTag armyList = nbt.getList("ServantArmy", Tag.TAG_COMPOUND);
            if (armyList.isEmpty()) return;
            selectedId = armyList.getCompound(0).getString("id");
        }
        ServantEntity servant = new ServantEntity(EntityRegistry.SERVANT_ENTITY.get(), level);
        servant.getPersistentData().putString("ServantType", selectedId);
        servant.setPos(player.getX(), player.getY(), player.getZ());
        servant.setOwner(player);
        servant.syncEquipmentFromRing(stack);
        level.addFreshEntity(servant);
        player.displayClientMessage(Component.translatable("message.ticex_annihilation.spawn").withStyle(ChatFormatting.GOLD), true);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            int nextMode = (getMode(stack) + 1) % 4;
            stack.getOrCreateTag().putInt("Mode", nextMode);
            String modeKey = switch (nextMode) {
                case MODE_FAMINE -> "message.ticex_annihilation.mode.equipment";
                case MODE_CONQUEST -> "message.ticex_annihilation.mode.servant";
                case MODE_WAR -> "message.ticex_annihilation.mode.war";
                case MODE_DEATH -> "message.ticex_annihilation.mode.death";
                default -> "unknown";
            };
            player.displayClientMessage(Component.translatable("message.ticex_annihilation.mode_switch", Component.translatable(modeKey)).withStyle(ChatFormatting.GOLD), true);
            if (level.isClientSide) player.playSound(SoundEvents.UI_BUTTON_CLICK.get(), 1.0f, 1.0f);
            return InteractionResultHolder.success(stack);
        }
        return InteractionResultHolder.pass(stack);
    }

    private void absorbMob(ItemStack ring, Player player, LivingEntity victim) {
        if (victim instanceof Player || !victim.isAlive() || victim.getPersistentData().getBoolean("IsServant")) return;
        CompoundTag nbt = ring.getOrCreateTag();
        ListTag armyList = nbt.getList("ServantArmy", Tag.TAG_COMPOUND);
        String entityId = Objects.requireNonNull(ForgeRegistries.ENTITY_TYPES.getKey(victim.getType())).toString();
        int currentCount = 1;
        boolean found = false;
        for (int i = 0; i < armyList.size(); i++) {
            CompoundTag entry = armyList.getCompound(i);
            if (entry.getString("id").equals(entityId)) {
                currentCount = entry.getInt("count") + 1;
                entry.putInt("count", currentCount);
                found = true;
                break;
            }
        }
        if (!found) {
            CompoundTag newEntry = new CompoundTag();
            newEntry.putString("id", entityId);
            newEntry.putInt("count", 1);
            armyList.add(newEntry);
        }
        nbt.put("ServantArmy", armyList);
        player.displayClientMessage(Component.translatable("message.ticex_annihilation.absorb", victim.getName(), currentCount).withStyle(ChatFormatting.DARK_PURPLE), true);
    }

    public void triggerWarFrenzy(Player player, ItemStack stack) {
        player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(500.0)).forEach(e -> {
            if (e == player || e.getPersistentData().getBoolean("IsServant")) e.getPersistentData().putInt("CalamityBlessingTicks", 1200);
        });
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDER_DRAGON_GROWL, player.getSoundSource(), 1.0F, 1.2F);
    }

    private void activateDeathAura(Player player, ItemStack ring) {
        CompoundTag nbt = ring.getOrCreateTag();
        if (nbt.getInt("DeathEssence") >= 20) {
            nbt.putInt("DeathEssence", nbt.getInt("DeathEssence") - 20);
            nbt.putInt("DeathAuraTicks", 1200);
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WITHER_SPAWN, player.getSoundSource(), 1.0f, 0.5f);
        }
    }

    @Override public @NotNull Component getDisplayName() { return Component.translatable("container.ticex_annihilation.calamity_ring"); }
    @Nullable @Override public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return new CalamityRingMenu(id, inv, player.getMainHandItem());
    }
}