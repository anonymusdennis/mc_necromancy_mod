package com.sirolf2009.necromancy.entity;

import com.sirolf2009.necromancy.item.NecromancyItems;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * EntityTear -- generic tear projectile (3 damage, splash particle).
 *
 * <p>Direct port of legacy {@code EntityTear}.  {@link #init(LivingEntity,
 * LivingEntity)} aims at a target with a fixed initial velocity, mirroring
 * the legacy three-arg constructor.
 */
public class EntityTear extends ThrowableItemProjectile {

    protected float damage = 3F;
    protected ParticleOptions particle = ParticleTypes.SPLASH;

    public EntityTear(EntityType<? extends EntityTear> type, Level level) {
        super(type, level);
    }

    @Override
    protected Item getDefaultItem() { return NecromancyItems.TEAR.get(); }

    /** Aim and launch at the given target, mirroring legacy projectile math. */
    public void init(LivingEntity shooter, LivingEntity target) {
        setOwner(shooter);
        moveTo(shooter.getX(), shooter.getEyeY() - 0.10, shooter.getZ(),
               shooter.getYRot(), shooter.getXRot());
        double dx = target.getX() - shooter.getX();
        double dy = (target.getBoundingBox().minY + target.getBbHeight() / 3.0F) - getY();
        double dz = target.getZ() - shooter.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        if (dist >= 1e-7) {
            shoot(dx, dy, dz, 1.0F, 2F);
        }
    }

    @Override
    protected void onHit(HitResult res) {
        if (res instanceof EntityHitResult ehr) {
            Entity hit = ehr.getEntity();
            hit.hurt(damageSources().thrown(this, getOwner()), damage);
        }
        for (int i = 0; i < 8; i++) {
            level().addParticle(particle, getX(), getY(), getZ(), 0, 0, 0);
        }
        if (!level().isClientSide) {
            discard();
        }
    }

    @Override
    protected double getDefaultGravity() { return 0.03; }
}
