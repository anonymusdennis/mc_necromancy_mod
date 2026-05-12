package com.sirolf2009.necromancy.entity;

import net.minecraft.client.particle.Particle;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class EntityNecroFX extends Particle
{
    public EntityNecroFX(World world, double x, double y, double z,
            double motionX, double motionY, double motionZ)
    {
        super(world, x, y, z, motionX, motionY, motionZ);
        this.motionX = motionX;
        this.motionY = motionY;
        this.motionZ = motionZ;
        particleMaxAge = 20;
        particleRed = 0.8F;
        particleGreen = 0.1F;
        particleBlue = 0.1F;
        particleScale = 0.3F;
    }

    @Override
    public void onUpdate()
    {
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;
        if (particleAge++ >= particleMaxAge) setExpired();
        move(motionX, motionY, motionZ);
        motionX *= 0.96F;
        motionY *= 0.96F;
        motionZ *= 0.96F;
    }

    @Override
    public int getFXLayer() { return 0; }
}
