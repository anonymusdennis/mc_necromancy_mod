package com.sirolf2009.necromancy.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;

public class BlockBlood extends BlockFluidClassic
{
    public BlockBlood(Fluid fluid)
    {
        super(fluid, Material.WATER);
    }

    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }
}
