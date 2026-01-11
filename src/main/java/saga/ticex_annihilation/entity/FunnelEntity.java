package saga.ticex_annihilation.entity;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.entity.shooter.ShooterDataHolder;
import com.tacz.guns.item.ModernKineticGunItem;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import saga.ticex_annihilation.registries.ModifierRegistry;
import slimeknights.tconstruct.library.tools.item.IModifiableDisplay;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

import java.util.Comparator;
import java.util.Optional;

public class FunnelEntity extends Entity {
    private static final EntityDataAccessor<ItemStack> DATA_ITEM = SynchedEntityData.defineId(FunnelEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Optional<java.util.UUID>> DATA_OWNER = SynchedEntityData.defineId(FunnelEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    private static final double SCAN_RANGE = 100.0;
    private int funnelIndex = 0;
    private int attackCooldown = 0;
    private LivingEntity owner;

    public FunnelEntity(EntityType<?> type, Level level) { super(type, level); }

    @Override protected void defineSynchedData() {
        this.entityData.define(DATA_ITEM, ItemStack.EMPTY);
        this.entityData.define(DATA_OWNER, Optional.empty());
    }

    public void setStoredItem(ItemStack stack) {
        this.entityData.set(DATA_ITEM, stack.copy());
    }

    public ItemStack getStoredItem() { return this.entityData.get(DATA_ITEM); }
    public void setOwner(LivingEntity owner) { this.owner = owner; this.entityData.set(DATA_OWNER, Optional.of(owner.getUUID())); }
    public LivingEntity getOwner() {
        if (owner == null) this.entityData.get(DATA_OWNER).ifPresent(uuid -> owner = level().getPlayerByUUID(uuid));
        return owner;
    }
    public void setFunnelIndex(int index) { this.funnelIndex = index; }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;
        LivingEntity owner = getOwner();
        if (owner == null || !owner.isAlive()) { this.discard(); return; }

        findTarget().ifPresentOrElse(target -> {
            attackMove(target);
            if (attackCooldown <= 0) executeAttack(target, owner);
            else attackCooldown--;
        }, () -> idleMove(owner));
    }

    private void attackMove(LivingEntity target) {
        double dist = this.distanceTo(target);
        double moveSpeed = dist > 20 ? 4.0 : 1.2;
        double time = level().getGameTime() * 0.4;
        Vec3 orbit = new Vec3(Math.cos(time + funnelIndex) * 4, 4.5, Math.sin(time + funnelIndex) * 4);
        Vec3 goalPos = target.position().add(orbit);
        this.setDeltaMovement(goalPos.subtract(position()).normalize().scale(moveSpeed));
        this.move(net.minecraft.world.entity.MoverType.SELF, getDeltaMovement());
        this.lookAt(EntityAnchorArgument.Anchor.EYES, target.getEyePosition());
    }

    private void idleMove(LivingEntity owner) {
        double angle = (level().getGameTime() * 0.25) + (funnelIndex * 0.8);
        Vec3 goalPos = owner.position().add(Math.cos(angle) * 3, 2.5, Math.sin(angle) * 3);
        this.setDeltaMovement(goalPos.subtract(position()).scale(0.3));
        this.move(net.minecraft.world.entity.MoverType.SELF, getDeltaMovement());
    }

    private void executeAttack(LivingEntity target, LivingEntity owner) {
        ItemStack stack = getStoredItem();
        ToolStack tool = ToolStack.from(stack);

        // 次元連結補給（無限供給）のチェック
        boolean isInfinite = tool.getModifierLevel(ModifierRegistry.ETERNAL_SUPPLY.get()) > 0;

        // 1. TaCZ 射撃
        if (stack.getItem() instanceof ModernKineticGunItem gunItem) {
            int currentAmmo = ((IGun) gunItem).getCurrentAmmoCount(stack);

            if (currentAmmo <= 0 && !isInfinite) {
                recallToOwner();
                return;
            }

        }
        // 2. Iron's Spellbooks 魔法
        else if (ISpellContainer.isSpellContainer(stack)) {
            handleMagicAttack(target, owner, stack, isInfinite);
        }
        // 3. Tinkers' 近接
        else if (stack.getItem() instanceof IModifiableDisplay) {
            handleTinkersAttack(target, owner, stack);
        }
    }

    private void handleMagicAttack(LivingEntity target, LivingEntity owner, ItemStack stack, boolean isInfinite) {
        ISpellContainer container = ISpellContainer.get(stack);
        if (!(owner instanceof Player player)) return;

        container.getActiveSpells().stream().findFirst().ifPresent(spellData -> {
            MagicData magicData = MagicData.getPlayerMagicData(player);
            AbstractSpell spell = spellData.getSpell();

            // 引数を 1 つに修正 (プロジェクト環境依存)
            int cost = spell.getManaCost(spellData.getLevel());

            if (magicData.getMana() < cost && !isInfinite) {
                recallToOwner();
                return;
            }

            spell.onCast(level(), spellData.getLevel(), player, CastSource.SWORD, magicData);
            if (!isInfinite) magicData.setMana(magicData.getMana() - cost);
            this.attackCooldown = 20;
        });
    }

    private void handleTinkersAttack(LivingEntity target, LivingEntity owner, ItemStack stack) {
        if (owner instanceof Player player && this.distanceTo(target) < 5.0) {
            float damage = ToolStack.from(stack).getStats().get(ToolStats.ATTACK_DAMAGE);
            target.hurt(level().damageSources().playerAttack(player), damage);
            this.attackCooldown = 5;
        }
    }

    private void recallToOwner() {
        if (getOwner() instanceof Player player) {
            player.displayClientMessage(Component.translatable("message.ticex_annihilation.out_of_resource", getStoredItem().getHoverName()).withStyle(ChatFormatting.RED), true);
        }
        this.discard();
    }

    private Optional<LivingEntity> findTarget() {
        return level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(SCAN_RANGE),
                        e -> e != owner && e.isAlive() && !e.isAlliedTo(owner))
                .stream().min(Comparator.comparingDouble(this::distanceToSqr));
    }

    @Override protected void readAdditionalSaveData(CompoundTag nbt) { if (nbt.contains("Item")) setStoredItem(ItemStack.of(nbt.getCompound("Item"))); }
    @Override protected void addAdditionalSaveData(CompoundTag nbt) { nbt.put("Item", getStoredItem().save(new CompoundTag())); }
}