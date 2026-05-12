package com.sirolf2009.necromancy.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class EntityTear extends EntityThrowable
{
    public EntityTear(World world) { super(world); }

    public EntityTear(World world, EntityLivingBase thrower)
    {
        super(world, thrower);
    }

    public EntityTear(World world, EntityLivingBase thrower, EntityLivingBase target)
    {
        super(world, thrower);
        double dx = target.posX - thrower.posX;
        double dy = target.posY + target.getEyeHeight() - 1.1D - posY;
        double dz = target.posZ - thrower.posZ;
        double dist = Math.sqrt(dx * dx + dz * dz);
        shoot(dx, dy + dist * 0.2D, dz, 1.6F, 1.0F);
    }

    @Override
    protected void onImpact(RayTraceResult result)
    {
        if (result.entityHit != null && getThrower() != null)
        {
            result.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, getThrower()), 3.0F);
        }
        if (!world.isRemote) setDead();
    }

    @Override
    public float getGravityVelocity() { return 0.03F; }
}
