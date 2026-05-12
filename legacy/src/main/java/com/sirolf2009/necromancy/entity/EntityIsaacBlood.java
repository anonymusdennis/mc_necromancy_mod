package com.sirolf2009.necromancy.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackRanged;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

public class EntityIsaacBlood extends EntityIsaacBody implements IRangedAttackMob
{
    public EntityIsaacBlood(World world)
    {
        super(world);
        if (!world.isRemote)
            tasks.addTask(1, new EntityAIAttackRanged<>(this, 0.35D, 12, 40F));
    }

    @Override
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(40D);
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3D);
    }

    @Override
    public void attackEntityWithRangedAttack(EntityLivingBase target, float distanceFactor)
    {
        SoundEvent sound = SoundEvent.REGISTRY.getObject(new ResourceLocation("necromancy:tear"));
        if (sound != null) playSound(sound, 1.0F, 1.0F / (getRNG().nextFloat() * 0.4F + 0.8F));
        world.spawnEntity(rand.nextInt(3) == 0
                ? new EntityTearBlood(world, this, target)
                : new EntityTear(world, this, target));
    }

    @Override
    public void onDeath(DamageSource cause)
    {
        super.onDeath(cause);
        if (!world.isRemote)
        {
            EntityIsaacHead head = new EntityIsaacHead(world);
            head.setLocationAndAngles(posX, posY, posZ, rotationYaw, rotationPitch);
            world.spawnEntity(head);
        }
    }
}
