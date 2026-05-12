package com.sirolf2009.necromancy.generation;

import java.util.Random;

import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;

public class WorldGenerator implements IWorldGenerator
{
    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world,
            IChunkGenerator chunkGenerator, IChunkProvider chunkProvider)
    {
        int x = chunkX * 16 + random.nextInt(16);
        int y = random.nextInt(60);
        int z = chunkZ * 16 + random.nextInt(16);

        if (world.provider.getDimension() == -1)
        {
            new WorldGenNetherChalice().generate(world, random, x, y, z);
        }
    }
}
