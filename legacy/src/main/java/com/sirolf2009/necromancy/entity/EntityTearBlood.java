package com.sirolf2009.necromancy.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class EntityTearBlood extends EntityTear
{
    public EntityTearBlood(World world) { super(world); }

    public EntityTearBlood(World world, EntityLivingBase thrower)
    {
        super(world, thrower);
    }

    public EntityTearBlood(World world, EntityLivingBase thrower, EntityLivingBase target)
    {
        super(world, thrower, target);
    }

    @Override
    protected void onImpact(RayTraceResult result)
    {
        if (result.entityHit instanceof EntityLivingBase && getThrower() != null)
        {
            result.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, getThrower()), 5.0F);
            ((EntityLivingBase) result.entityHit).addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 60, 1));
        }
        if (!world.isRemote) setDead();
    }
}
