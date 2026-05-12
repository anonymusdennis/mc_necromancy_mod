package com.sirolf2009.necromancy.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.level.Level;

/**
 * EntityIsaacNormal -- ranged variant.  When killed, spawns
 * {@link EntityIsaacBlood} at the same location, mirroring the legacy
 * {@code EntityIsaacNormal#onDeath}.
 */
public class EntityIsaacNormal extends EntityIsaacBody implements RangedAttackMob {

    public EntityIsaacNormal(EntityType<? extends EntityIsaacNormal> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new RangedAttackGoal(this, 0.25, 18, 50F));
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distance) {
        var rl = com.sirolf2009.necromancy.Reference.rl("tear");
        var snd = BuiltInRegistries.SOUND_EVENT.get(rl);
        if (snd != null) playSound(snd, 1.0F, 1.0F / (random.nextFloat() * 0.4F + 0.8F));
        else             playSound(SoundEvents.GHAST_HURT, 1.0F, 1.0F);
        var t = new EntityTear(NecromancyEntities.TEAR.get(), level());
        t.init(this, target);
        level().addFreshEntity(t);
    }

    @Override
    protected SoundEvent getAmbientSound() { return SoundEvents.GHAST_AMBIENT; }
    @Override
    protected SoundEvent getHurtSound(DamageSource src) { return SoundEvents.GHAST_HURT; }

    @Override
    public void die(DamageSource src) {
        super.die(src);
        // Only the bare "Normal" stage upgrades into a Blood; subclasses
        // (Blood, Head) override this hook so we do not spawn forever.
        if (!level().isClientSide && this.getClass() == EntityIsaacNormal.class) {
            EntityIsaacBlood blood = NecromancyEntities.ISAAC_BLOOD.get().create(level());
            if (blood != null) {
                blood.moveTo(getX(), getY(), getZ(), getYRot(), getXRot());
                level().addFreshEntity(blood);
            }
        }
    }
}
