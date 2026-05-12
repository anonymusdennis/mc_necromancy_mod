package com.sirolf2009.necromancy.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;

import com.sirolf2009.necromancy.Necromancy;

public class BlockAltarBlock extends Block
{
    public BlockAltarBlock()
    {
        super(Material.ROCK);
        setCreativeTab(Necromancy.tabNecromancy);
    }

    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return true;
    }
}
