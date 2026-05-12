package com.sirolf2009.necromancy.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackRanged;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

public class EntityIsaacHead extends EntityIsaacBody implements IRangedAttackMob
{
    public EntityIsaacHead(World world)
    {
        super(world);
        setSize(0.4F, 0.4F);
        if (!world.isRemote)
            tasks.addTask(1, new EntityAIAttackRanged<>(this, 0.4D, 8, 30F));
    }

    @Override
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(15D);
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.35D);
    }

    @Override
    public void attackEntityWithRangedAttack(EntityLivingBase target, float distanceFactor)
    {
        SoundEvent sound = SoundEvent.REGISTRY.getObject(new ResourceLocation("necromancy:tear"));
        if (sound != null) playSound(sound, 1.0F, 1.0F / (getRNG().nextFloat() * 0.4F + 0.8F));
        world.spawnEntity(new EntityTearBlood(world, this, target));
    }
}
