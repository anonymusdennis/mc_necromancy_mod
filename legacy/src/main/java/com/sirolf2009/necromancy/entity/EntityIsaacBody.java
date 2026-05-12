package com.sirolf2009.necromancy.entity;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

public class EntityIsaacBody extends EntityMob
{
    public EntityIsaacBody(World world)
    {
        super(world);
        setSize(0.6F, 1.8F);
        tasks.addTask(1, new EntityAISwimming(this));
        tasks.addTask(2, new EntityAIAvoidEntity<>(this, EntityPlayer.class, 8.0F, 0.6D, 0.6D));
        tasks.addTask(3, new EntityAIWanderAvoidWater(this, 0.4D));
        tasks.addTask(4, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        tasks.addTask(5, new EntityAILookIdle(this));
        targetTasks.addTask(1, new EntityAINearestAttackableTarget<>(this, EntityPlayer.class, true));
        targetTasks.addTask(2, new EntityAIHurtByTarget(this, true));
    }

    @Override
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20D);
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
        getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(3D);
    }

    @Override
    protected SoundEvent getAmbientSound()
    {
        return SoundEvent.REGISTRY.getObject(new ResourceLocation("mob.ghast.moan"));
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source)
    {
        return SoundEvent.REGISTRY.getObject(new ResourceLocation("mob.ghast.scream"));
    }

    @Override
    protected SoundEvent getDeathSound()
    {
        return SoundEvent.REGISTRY.getObject(new ResourceLocation("mob.ghast.death"));
    }
}
