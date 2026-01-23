package saga.ticex_annihilation.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import saga.ticex_annihilation.entity.ai.ServantFollowOwnerGoal;
import saga.ticex_annihilation.item.CalamityRingItem;
import saga.ticex_annihilation.inventory.CalamityRingInventory;

import java.util.Optional;
import java.util.UUID;

public class ServantEntity extends PathfinderMob {
    private static final EntityDataAccessor<Integer> DATA_MODE = SynchedEntityData.defineId(ServantEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER = SynchedEntityData.defineId(ServantEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<String> ORIGINAL_ENTITY_ID = SynchedEntityData.defineId(ServantEntity.class, EntityDataSerializers.STRING);

    private int attackCooldown = 0;
    private static final UUID EQUIPMENT_MODIFIER_UUID = UUID.fromString("77777777-7777-7777-7777-777777777777");

    public ServantEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_MODE, 1);
        this.entityData.define(DATA_OWNER, Optional.empty());
        this.entityData.define(ORIGINAL_ENTITY_ID, "minecraft:zombie");
    }

    // --- 装備とステータスの同期ロジック ---
    public void syncEquipmentFromRing(ItemStack ring) {
        if (!(ring.getItem() instanceof CalamityRingItem)) return;
        CalamityRingInventory inv = new CalamityRingInventory(ring);

        // 支配GUIの専用スロット (200-204) から装備を取得して装着
        this.setItemSlot(EquipmentSlot.MAINHAND, inv.getStackInSlot(200).copy());
        this.setItemSlot(EquipmentSlot.HEAD, inv.getStackInSlot(201).copy());
        this.setItemSlot(EquipmentSlot.CHEST, inv.getStackInSlot(202).copy());
        this.setItemSlot(EquipmentSlot.LEGS, inv.getStackInSlot(203).copy());
        this.setItemSlot(EquipmentSlot.FEET, inv.getStackInSlot(204).copy());

        // ドロップ防止
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            this.setDropChance(slot, 0.0F);
        }

        // 攻撃力の適用：装備が無ければ加算なし
        applyEquipmentAttributes();
    }

    private void applyEquipmentAttributes() {
        var attackAttr = this.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackAttr == null) return;

        // 一旦古いバフを解除
        attackAttr.removeModifier(EQUIPMENT_MODIFIER_UUID);

        ItemStack weapon = this.getMainHandItem();
        if (!weapon.isEmpty()) {
            // 武器の基本攻撃力を抽出（簡易版：実際のツール値を参照）
            double weaponDamage = 4.0; // デフォルト値
            // ここで weapon.getAttributeModifiers などを参照して正確な値を出すことも可能

            attackAttr.addTransientModifier(new AttributeModifier(
                    EQUIPMENT_MODIFIER_UUID, "Calamity Equipment Bonus", weaponDamage, AttributeModifier.Operation.ADDITION));
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!level().isClientSide) {
            LivingEntity target = getTarget();
            LivingEntity owner = getOwner();

            if (target != null && target.isAlive() && owner instanceof Player player) {
                if (attackCooldown <= 0) {
                    executeCalamityAttack(target, player);
                } else {
                    attackCooldown--;
                }
            }
        }
    }

    private void executeCalamityAttack(LivingEntity target, Player owner) {
        // 現在装備している武器（syncEquipmentFromRingでセットされたもの）を参照
        ItemStack weapon = this.getMainHandItem();

        // 武器がない場合は素手ダメージ、あれば武器の性能
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);

        double dist = this.distanceToSqr(target);
        if (dist < 16.0) { // 近接攻撃範囲
            this.swing(InteractionHand.MAIN_HAND);
            target.hurt(this.level().damageSources().mobAttack(this), damage);
            this.attackCooldown = 15;
        } else {
            // 遠距離攻撃（銃器/魔法）が必要な場合はここに以前のロジックを戻す
            this.attackCooldown = 20;
        }
    }

    // --- 以下、既存のゴール登録やNBT処理 ---
    @Override
    protected void registerGoals() {
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Mob.class, 10, true, false,
                (target) -> target.isAlive() && !target.getUUID().equals(getOwnerUUID().orElse(null))));
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new ServantFollowOwnerGoal(this, 1.2D, 5.0F, 2.0F));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0D));
    }

    @Nullable
    public LivingEntity getOwner() {
        return getOwnerUUID().map(uuid -> {
            if (!this.level().isClientSide) return (LivingEntity) ((net.minecraft.server.level.ServerLevel) this.level()).getEntity(uuid);
            return (LivingEntity) this.level().getPlayerByUUID(uuid);
        }).orElse(null);
    }

    public int getMode() { return this.entityData.get(DATA_MODE); }
    public Optional<UUID> getOwnerUUID() { return this.entityData.get(DATA_OWNER); }
    public void setOwner(Player player) { this.entityData.set(DATA_OWNER, Optional.of(player.getUUID())); }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putInt("ServantMode", this.getMode());
        getOwnerUUID().ifPresent(uuid -> nbt.putUUID("OwnerUUID", uuid));
        nbt.putString("OriginalID", this.entityData.get(ORIGINAL_ENTITY_ID));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        this.entityData.set(DATA_MODE, nbt.getInt("ServantMode"));
        if (nbt.hasUUID("OwnerUUID")) this.entityData.set(DATA_OWNER, Optional.of(nbt.getUUID("OwnerUUID")));
        this.entityData.set(ORIGINAL_ENTITY_ID, nbt.getString("OriginalID"));
    }
}