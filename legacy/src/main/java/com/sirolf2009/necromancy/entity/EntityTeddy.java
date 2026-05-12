package com.sirolf2009.necromancy.entity;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

public class EntityTeddy extends EntityMob
{
    public EntityTeddy(World world)
    {
        super(world);
        setSize(0.6F, 1.4F);
        tasks.addTask(1, new EntityAISwimming(this));
        tasks.addTask(2, new EntityAIAttackMelee(this, 0.4D, true));
        tasks.addTask(3, new EntityAIWanderAvoidWater(this, 0.35D));
        tasks.addTask(4, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        tasks.addTask(5, new EntityAILookIdle(this));
        targetTasks.addTask(1, new EntityAINearestAttackableTarget<>(this, EntityPlayer.class, true));
        targetTasks.addTask(2, new EntityAIHurtByTarget(this, true));
    }

    @Override
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(30D);
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.2D);
        getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6D);
    }

    @Override
    protected SoundEvent getAmbientSound()
    {
        return SoundEvent.REGISTRY.getObject(new ResourceLocation("mob.bear.idle"));
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source)
    {
        return SoundEvent.REGISTRY.getObject(new ResourceLocation("mob.bear.hurt"));
    }

    @Override
    protected SoundEvent getDeathSound()
    {
        return SoundEvent.REGISTRY.getObject(new ResourceLocation("mob.bear.death"));
    }
}
