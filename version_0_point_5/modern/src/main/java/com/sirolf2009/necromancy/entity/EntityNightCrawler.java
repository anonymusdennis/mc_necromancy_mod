package com.sirolf2009.necromancy.entity;

import com.sirolf2009.necromancy.Reference;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FleeSunGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RestrictSunGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * EntityNightCrawler -- monster spawned in place of zombies (configurable rarity).
 *
 * <p>Fast, sun-fearing zombie variant.  Hits apply Wither II for ~6s and use
 * the modded {@code nightcrawler.howl}/{@code .scream} ambient sounds.
 * Attribute defaults are 35 HP, 0.30 movement speed, 4 attack damage; matches
 * legacy {@code EntityNightCrawler}.
 */
public class EntityNightCrawler extends Monster {

    public EntityNightCrawler(EntityType<? extends EntityNightCrawler> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        float moveSpeed = 0.25F;
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(3, new RestrictSunGoal(this));
        this.goalSelector.addGoal(4, new FleeSunGoal(this, moveSpeed));
        this.goalSelector.addGoal(5, new RandomStrollGoal(this, moveSpeed));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 35.0)
            .add(Attributes.FOLLOW_RANGE, 32.0)
            .add(Attributes.KNOCKBACK_RESISTANCE, 0.0)
            .add(Attributes.MOVEMENT_SPEED, 0.30)
            .add(Attributes.ATTACK_DAMAGE, 4.0);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean flag = super.doHurtTarget(target);
        if (flag && target instanceof LivingEntity le) {
            le.addEffect(new MobEffectInstance(MobEffects.WITHER, 120, 0));
        }
        return flag;
    }

    private static final ResourceLocation HOWL_ID   = Reference.rl("nightcrawler.howl");
    private static final ResourceLocation SCREAM_ID = Reference.rl("nightcrawler.scream");

    @Override
    protected SoundEvent getAmbientSound() {
        var howl   = BuiltInRegistries.SOUND_EVENT.get(HOWL_ID);
        var scream = BuiltInRegistries.SOUND_EVENT.get(SCREAM_ID);
        return random.nextBoolean() ? (howl != null ? howl : SoundEvents.ZOMBIE_AMBIENT)
                                    : (scream != null ? scream : SoundEvents.ZOMBIE_AMBIENT);
    }
    @Override
    protected SoundEvent getHurtSound(DamageSource src) { return SoundEvents.ENDERMAN_HURT; }
    @Override
    protected SoundEvent getDeathSound() { return SoundEvents.ENDERMAN_DEATH; }
}
