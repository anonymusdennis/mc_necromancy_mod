package com.sirolf2009.necromancy.generation;

import java.util.List;
import java.util.Random;

import net.minecraft.world.gen.structure.StructureVillagePieces;
import net.minecraftforge.fml.common.registry.VillagerRegistry;

import com.sirolf2009.necromancy.generation.villagecomponent.ComponentVillageCemetery;

public class VillageCreationHandler implements VillagerRegistry.IVillageCreationHandler
{
    @Override
    public StructureVillagePieces.PieceWeight getVillagePieceWeight(Random random, int i)
    {
        return new StructureVillagePieces.PieceWeight(ComponentVillageCemetery.class, 5, 1);
    }

    @Override
    public Class<?> getComponentClass()
    {
        return ComponentVillageCemetery.class;
    }

    @Override
    public Object buildComponent(StructureVillagePieces.PieceWeight villagePiece,
            StructureVillagePieces.Start startPiece, List<net.minecraft.world.gen.structure.StructureComponent> pieces,
            Random random, int p1, int p2, int p3, net.minecraft.util.EnumFacing facing, int p5)
    {
        return ComponentVillageCemetery.buildComponent(startPiece, pieces, random, p1, p2, p3, facing, p5);
    }
}
