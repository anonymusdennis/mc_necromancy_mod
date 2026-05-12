package com.sirolf2009.necromancy.generation;

import java.util.Random;

import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

import com.sirolf2009.necromancy.block.RegistryBlocksNecromancy;

public class WorldGenNetherChalice extends WorldGenerator
{
    @Override
    public boolean generate(World world, Random rand, BlockPos pos)
    {
        return generate(world, rand, pos.getX(), pos.getY(), pos.getZ());
    }

    public boolean generate(World world, Random rand, int x, int y, int z)
    {
        for (int attempts = 0; attempts < 16; attempts++)
        {
            int px = x + rand.nextInt(8) - rand.nextInt(8);
            int py = y + rand.nextInt(4) - rand.nextInt(4);
            int pz = z + rand.nextInt(8) - rand.nextInt(8);

            if (world.isAirBlock(new BlockPos(px, py, pz))
                    && !world.isAirBlock(new BlockPos(px, py - 1, pz)))
            {
                // Build a simple blood chalice structure: 3 blocks tall cross pattern filled with blood
                if (RegistryBlocksNecromancy.blood != null)
                {
                    world.setBlockState(new BlockPos(px, py, pz),
                            RegistryBlocksNecromancy.blood.getDefaultState(), 2);
                    world.setBlockState(new BlockPos(px + 1, py, pz),
                            Blocks.SOUL_SAND.getDefaultState(), 2);
                    world.setBlockState(new BlockPos(px - 1, py, pz),
                            Blocks.SOUL_SAND.getDefaultState(), 2);
                    world.setBlockState(new BlockPos(px, py, pz + 1),
                            Blocks.SOUL_SAND.getDefaultState(), 2);
                    world.setBlockState(new BlockPos(px, py, pz - 1),
                            Blocks.SOUL_SAND.getDefaultState(), 2);
                }
                return true;
            }
        }
        return false;
    }
}
