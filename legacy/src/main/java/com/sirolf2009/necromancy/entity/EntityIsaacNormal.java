package com.sirolf2009.necromancy.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.ai.EntityAIAttackRanged;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

public class EntityIsaacNormal extends EntityIsaacBody implements IRangedAttackMob
{
    public EntityIsaacNormal(World world)
    {
        super(world);
        if (!world.isRemote)
            tasks.addTask(1, new EntityAIAttackRanged<>(this, 0.25D, 18, 50F));
    }

    @Override
    public void attackEntityWithRangedAttack(EntityLivingBase target, float distanceFactor)
    {
        SoundEvent sound = SoundEvent.REGISTRY.getObject(new ResourceLocation("necromancy:tear"));
        if (sound != null) playSound(sound, 1.0F, 1.0F / (getRNG().nextFloat() * 0.4F + 0.8F));
        world.spawnEntity(new EntityTear(world, this, target));
    }

    @Override
    public void onDeath(DamageSource cause)
    {
        super.onDeath(cause);
        if (!world.isRemote)
        {
            EntityIsaacBlood isaac = new EntityIsaacBlood(world);
            isaac.setLocationAndAngles(posX, posY, posZ, rotationYaw, rotationPitch);
            world.spawnEntity(isaac);
        }
    }
}
