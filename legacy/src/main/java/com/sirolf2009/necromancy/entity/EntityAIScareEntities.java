package com.sirolf2009.necromancy.entity;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;

import java.util.List;

public class EntityAIScareEntities extends EntityAIBase
{
    private final EntityLivingBase entity;
    private final Class<? extends EntityCreature> targetClass;
    private final float range;

    public EntityAIScareEntities(EntityLivingBase entity, Class<? extends EntityCreature> targetClass, float range)
    {
        this.entity = entity;
        this.targetClass = targetClass;
        this.range = range;
        setMutexBits(1);
    }

    @Override
    public boolean shouldExecute()
    {
        return !entity.world.isRemote && !entity.world.getEntitiesWithinAABB(targetClass,
                entity.getEntityBoundingBox().grow(range, range, range)).isEmpty();
    }

    @Override
    public void updateTask()
    {
        List<? extends EntityCreature> targets = entity.world.getEntitiesWithinAABB(targetClass,
                entity.getEntityBoundingBox().grow(range, range, range));
        for (EntityCreature target : targets)
        {
            if (target.getNavigator() != null)
                target.getNavigator().tryMoveToXYZ(
                        target.posX + (target.posX - entity.posX) * 2,
                        target.posY,
                        target.posZ + (target.posZ - entity.posZ) * 2,
                        1.5D);
        }
    }
}
