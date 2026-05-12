package com.sirolf2009.necromancy.entity;

import com.sirolf2009.necromancy.api.BodyPartLocation;
import com.sirolf2009.necromancy.api.NecroEntityBase;
import com.sirolf2009.necromancy.api.NecroEntityRegistry;
import com.sirolf2009.necromancy.item.NecromancyItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import org.jetbrains.annotations.Nullable;

/**
 * EntityMinion -- the player's tamable necromantic servant.
 *
 * <p>Direct port of {@code EntityMinion}.  The mob:
 * <ul>
 *     <li>extends {@link TamableAnimal} -- inheritance from {@code EntityTameable}</li>
 *     <li>tracks 5 {@link BodyPartLocation} -> mob name strings via
 *         {@link SynchedEntityData} so the renderer can rebuild the correct
 *         model server-to-client.</li>
 *     <li>follows owner, hurts owner's targets, sits when ordered, and attacks
 *         nearby Monsters.</li>
 * </ul>
 *
 * <p>The body-part metadata mirrors the legacy {@code dataWatcherUpdate}
 * pattern: each location has a synced string with the mob name (e.g. "Cow").
 * Rendering is performed by {@code RenderMinion} (see renderers step).
 */
public class EntityMinion extends TamableAnimal {

    private static final EntityDataAccessor<String> DATA_HEAD       = SynchedEntityData.defineId(EntityMinion.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> DATA_TORSO      = SynchedEntityData.defineId(EntityMinion.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> DATA_ARM_LEFT   = SynchedEntityData.defineId(EntityMinion.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> DATA_ARM_RIGHT  = SynchedEntityData.defineId(EntityMinion.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> DATA_LEGS       = SynchedEntityData.defineId(EntityMinion.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> DATA_SADDLED   = SynchedEntityData.defineId(EntityMinion.class, EntityDataSerializers.BOOLEAN);

    public EntityMinion(EntityType<? extends EntityMinion> type, Level level) {
        super(type, level);
        this.setTame(false, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(6, new net.minecraft.world.entity.ai.goal.FollowOwnerGoal(this, 1.0, 10F, 2F));
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Monster.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return TamableAnimal.createMobAttributes()
            .add(Attributes.MAX_HEALTH,    20)
            .add(Attributes.MOVEMENT_SPEED, 0.30)
            .add(Attributes.FOLLOW_RANGE,  16)
            .add(Attributes.ATTACK_DAMAGE,  2)
            .add(Attributes.KNOCKBACK_RESISTANCE, 0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_HEAD,      "");
        builder.define(DATA_TORSO,     "");
        builder.define(DATA_ARM_LEFT,  "");
        builder.define(DATA_ARM_RIGHT, "");
        builder.define(DATA_LEGS,      "");
        builder.define(DATA_SADDLED,   false);
    }

    public void setBodyPartName(BodyPartLocation loc, String name) {
        switch (loc) {
            case Head     -> entityData.set(DATA_HEAD, name);
            case Torso    -> entityData.set(DATA_TORSO, name);
            case ArmLeft  -> entityData.set(DATA_ARM_LEFT, name);
            case ArmRight -> entityData.set(DATA_ARM_RIGHT, name);
            case Legs     -> entityData.set(DATA_LEGS, name);
        }
    }

    public String getBodyPartName(BodyPartLocation loc) {
        return switch (loc) {
            case Head     -> entityData.get(DATA_HEAD);
            case Torso    -> entityData.get(DATA_TORSO);
            case ArmLeft  -> entityData.get(DATA_ARM_LEFT);
            case ArmRight -> entityData.get(DATA_ARM_RIGHT);
            case Legs     -> entityData.get(DATA_LEGS);
        };
    }

    public NecroEntityBase getBodyPart(BodyPartLocation loc) {
        String name = getBodyPartName(loc);
        return name.isEmpty() ? null : NecroEntityRegistry.get(name);
    }

    public boolean isSaddled() { return entityData.get(DATA_SADDLED); }
    public void setSaddled(boolean v) { entityData.set(DATA_SADDLED, v); }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("Head",     entityData.get(DATA_HEAD));
        tag.putString("Torso",    entityData.get(DATA_TORSO));
        tag.putString("ArmLeft",  entityData.get(DATA_ARM_LEFT));
        tag.putString("ArmRight", entityData.get(DATA_ARM_RIGHT));
        tag.putString("Legs",     entityData.get(DATA_LEGS));
        tag.putBoolean("Saddled", isSaddled());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(DATA_HEAD,      tag.getString("Head"));
        entityData.set(DATA_TORSO,     tag.getString("Torso"));
        entityData.set(DATA_ARM_LEFT,  tag.getString("ArmLeft"));
        entityData.set(DATA_ARM_RIGHT, tag.getString("ArmRight"));
        entityData.set(DATA_LEGS,      tag.getString("Legs"));
        setSaddled(tag.getBoolean("Saddled"));
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return null; // minions cannot breed
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(NecromancyItems.BRAIN_ON_STICK.get());
    }

    @Override
    protected SoundEvent getAmbientSound() { return SoundEvents.ZOMBIE_AMBIENT; }
    @Override
    protected SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource src) { return SoundEvents.ZOMBIE_HURT; }
    @Override
    protected SoundEvent getDeathSound() { return SoundEvents.ZOMBIE_DEATH; }
}
