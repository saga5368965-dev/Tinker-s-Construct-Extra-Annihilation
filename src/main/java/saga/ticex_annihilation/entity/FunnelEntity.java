package saga.ticex_annihilation.entity;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.item.ModernKineticGunItem;
import com.tacz.guns.util.AttachmentDataUtils;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ParticleTypes;
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

    public void setStoredItem(ItemStack stack) { this.entityData.set(DATA_ITEM, stack.copy()); }
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
        LivingEntity currentOwner = getOwner();
        if (currentOwner == null || !currentOwner.isAlive()) { this.discard(); return; }

        findTarget().ifPresentOrElse(target -> {
            attackMove(target);
            if (attackCooldown <= 0) executeAttack(target, currentOwner);
            else attackCooldown--;
        }, () -> idleMove(currentOwner));
    }

    private void attackMove(LivingEntity target) {
        double time = level().getGameTime() * 0.6;
        Vec3 orbit = new Vec3(
                Math.cos(time + funnelIndex) * 6,
                3.5 + Math.sin(time * 0.4) * 2,
                Math.sin(time + funnelIndex) * 6
        );
        Vec3 goalPos = target.position().add(orbit);

        if (this.position().distanceToSqr(goalPos) > 1.5) {
            if (level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.PORTAL, getX(), getY() + 1, getZ(), 5, 0.2, 0.2, 0.2, 0.02);
            }
            this.setPos(goalPos.x, goalPos.y, goalPos.z);
        }
        this.lookAt(EntityAnchorArgument.Anchor.EYES, target.getEyePosition());
    }

    private void idleMove(LivingEntity currentOwner) {
        double angle = (level().getGameTime() * 0.25) + (funnelIndex * 0.8);
        Vec3 goalPos = currentOwner.position().add(Math.cos(angle) * 3, 2.5, Math.sin(angle) * 3);
        this.setDeltaMovement(goalPos.subtract(position()).scale(0.3));
        this.move(net.minecraft.world.entity.MoverType.SELF, getDeltaMovement());
    }

    private void executeAttack(LivingEntity target, LivingEntity currentOwner) {
        ItemStack stack = getStoredItem();
        if (stack.isEmpty()) return;

        ToolStack tool = ToolStack.from(stack);
        // ETERNAL_SUPPLYがあるなら、リソースの有無に関わらず「常にリロード済み」として扱う
        boolean isInfinite = tool.getModifierLevel(ModifierRegistry.ETERNAL_SUPPLY.get()) > 0;

        if (stack.getItem() instanceof ModernKineticGunItem gunItem) {
            TimelessAPI.getCommonGunIndex(gunItem.getGunId(stack)).ifPresent(index -> {
                if (isInfinite) {
                    // 銃弾の概念を消去: 常にフルチャージして発射
                    int maxAmmo = AttachmentDataUtils.getAmmoCountWithAttachment(stack, index.getGunData());
                    gunItem.setCurrentAmmoCount(stack, maxAmmo);
                    gunItem.setBulletInBarrel(stack, true);
                    performShoot(target, currentOwner);
                } else if (gunItem.getCurrentAmmoCount(stack) > 0) {
                    performShoot(target, currentOwner);
                }
            });
        }
        else if (ISpellContainer.isSpellContainer(stack)) {
            handleMagicAttack(target, currentOwner, stack, isInfinite);
        }
        else if (stack.getItem() instanceof IModifiableDisplay) {
            handleTinkersAttack(target, currentOwner, stack);
        }
    }

    private void performShoot(LivingEntity target, LivingEntity currentOwner) {
        this.lookAt(EntityAnchorArgument.Anchor.EYES, target.getEyePosition());
        if (currentOwner instanceof Player) {
            // ここでTaCZの弾丸を生成。弾数はModifier側が固定し続けるので減らない
            this.attackCooldown = 5; // 無限なので高速連射
        }
    }

    private void handleMagicAttack(LivingEntity target, LivingEntity currentOwner, ItemStack stack, boolean isInfinite) {
        ISpellContainer container = ISpellContainer.get(stack);
        if (!(currentOwner instanceof Player player)) return;

        container.getActiveSpells().stream().findFirst().ifPresent(spellData -> {
            MagicData magicData = MagicData.getPlayerMagicData(player);
            AbstractSpell spell = spellData.getSpell();

            if (isInfinite) {
                // マナの概念を消去: 常にコスト以上のマナを一時的に保持させて踏み倒す
                int cost = spell.getManaCost(spellData.getLevel());
                if (magicData.getMana() < cost) magicData.setMana(cost);

                this.lookAt(EntityAnchorArgument.Anchor.EYES, target.getEyePosition());
                spell.onCast(level(), spellData.getLevel(), player, CastSource.NONE, magicData);

                // 撃った瞬間にマナを戻すことで「減らない」を表現
                magicData.setMana(magicData.getMana() + cost);
                this.attackCooldown = 10;
            } else if (magicData.getMana() >= spell.getManaCost(spellData.getLevel())) {
                spell.onCast(level(), spellData.getLevel(), player, CastSource.NONE, magicData);
                this.attackCooldown = 20;
            }
        });
    }

    private void handleTinkersAttack(LivingEntity target, LivingEntity currentOwner, ItemStack stack) {
        if (currentOwner instanceof Player player && this.distanceTo(target) < 5.0) {
            float damage = ToolStack.from(stack).getStats().get(ToolStats.ATTACK_DAMAGE);
            target.hurt(level().damageSources().playerAttack(player), damage);
            this.attackCooldown = 5;
        }
    }

    private Optional<LivingEntity> findTarget() {
        return level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(SCAN_RANGE),
                        e -> e != owner && e.isAlive() && !e.isAlliedTo(owner))
                .stream().min(Comparator.comparingDouble(this::distanceToSqr));
    }

    @Override protected void readAdditionalSaveData(CompoundTag nbt) { if (nbt.contains("Item")) setStoredItem(ItemStack.of(nbt.getCompound("Item"))); }
    @Override protected void addAdditionalSaveData(CompoundTag nbt) { nbt.put("Item", getStoredItem().save(new CompoundTag())); }
}