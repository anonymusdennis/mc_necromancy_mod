package com.sirolf2009.necromancy.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * EntityTeddy -- tamed companion that toggles between WALKING/DEFENDING/SITTING.
 *
 * <p>Direct port of the legacy {@code EntityTeddy}.  Cycles through three modes
 * on right-click and prints status to the owner.  The "scare" goal (hostile
 * mob avoidance) is {@link EntityAIScareEntities}.
 */
public class EntityTeddy extends TamableAnimal {

    public enum EntityState { WALKING, DEFENDING, SITTING }

    private static final EntityDataAccessor<Integer> DATA_STATE =
        SynchedEntityData.defineId(EntityTeddy.class, EntityDataSerializers.INT);

    public EntityTeddy(EntityType<? extends EntityTeddy> type, Level level) {
        super(type, level);
        this.setTame(true, false);
        this.setOrderedToSit(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return TamableAnimal.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 8.0)
            .add(Attributes.FOLLOW_RANGE, 32.0)
            .add(Attributes.MOVEMENT_SPEED, 0.30);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(3, new FollowOwnerGoal(this, 0.30, 8F, 5F));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 10F));
        this.goalSelector.addGoal(6, new EntityAIScareEntities(this, 10F, 7F));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_STATE, 0);
    }

    public EntityState getEntityState() {
        return EntityState.values()[entityData.get(DATA_STATE)];
    }
    public void setEntityState(EntityState s) {
        entityData.set(DATA_STATE, s.ordinal());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("state", entityData.get(DATA_STATE));
    }
    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(DATA_STATE, tag.getInt("state"));
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (level().isClientSide) return InteractionResult.SUCCESS;
        this.tame(player);
        EntityState next = switch (getEntityState()) {
            case WALKING   -> EntityState.DEFENDING;
            case DEFENDING -> EntityState.SITTING;
            case SITTING   -> EntityState.WALKING;
        };
        setEntityState(next);
        setOrderedToSit(next != EntityState.WALKING);
        player.sendSystemMessage(Component.literal("Animated Teddy is now " + next.name().toLowerCase()));
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        return target.hurt(damageSources().mobAttack(this), 3F);
    }

    @Override
    public boolean removeWhenFarAway(double distance) { return false; }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        return null;
    }

    @Override
    public boolean isFood(ItemStack stack) { return stack.is(Items.LEATHER); }

    @Override protected SoundEvent getAmbientSound() { return SoundEvents.ZOMBIE_AMBIENT; }
    @Override protected SoundEvent getHurtSound(DamageSource src) { return SoundEvents.ZOMBIE_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.ZOMBIE_DEATH; }
}
