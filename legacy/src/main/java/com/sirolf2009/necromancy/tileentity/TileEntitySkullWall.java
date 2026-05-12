package com.sirolf2009.necromancy.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;

public class TileEntitySkullWall extends TileEntity
{
    private String base = "";
    private String skull1 = "";
    private String skull2 = "";
    private String skull3 = "";

    @Override
    public AxisAlignedBB getRenderBoundingBox()
    {
        return TileEntity.INFINITE_EXTENT_AABB;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        base = compound.getString("Base");
        skull1 = compound.getString("Skull1");
        skull2 = compound.getString("Skull2");
        skull3 = compound.getString("Skull3");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        compound.setString("Base", base);
        compound.setString("Skull1", skull1);
        compound.setString("Skull2", skull2);
        compound.setString("Skull3", skull3);
        return compound;
    }

    public String getBase() { return base; }
    public String getSkull1() { return skull1; }
    public String getSkull2() { return skull2; }
    public String getSkull3() { return skull3; }

    public void setData(String base, String skull1, String skull2, String skull3)
    {
        this.base = base;
        this.skull1 = skull1;
        this.skull2 = skull2;
        this.skull3 = skull3;
        markDirty();
    }
}
