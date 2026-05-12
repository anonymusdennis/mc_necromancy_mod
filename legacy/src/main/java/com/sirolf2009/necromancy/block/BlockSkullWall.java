package com.sirolf2009.necromancy.block;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.sirolf2009.necromancy.Necromancy;
import com.sirolf2009.necromancy.tileentity.TileEntitySkullWall;

public class BlockSkullWall extends BlockContainer
{
    public BlockSkullWall()
    {
        super(Material.ROCK);
        setCreativeTab(Necromancy.tabNecromancy);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta)
    {
        return new TileEntitySkullWall();
    }

    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state)
    {
        return false;
    }
}
