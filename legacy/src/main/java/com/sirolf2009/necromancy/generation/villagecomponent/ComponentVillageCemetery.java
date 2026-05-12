package com.sirolf2009.necromancy.generation.villagecomponent;

import java.util.List;
import java.util.Random;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureVillagePieces;

import com.sirolf2009.necroapi.NecroEntityBase;
import com.sirolf2009.necroapi.NecroEntityRegistry;
import com.sirolf2009.necromancy.entity.RegistryNecromancyEntities;
import com.sirolf2009.necromancy.item.ItemBodyPart;
import com.sirolf2009.necromancy.item.RegistryNecromancyItems;

public class ComponentVillageCemetery extends StructureVillagePieces.Village
{
    private int averageGroundLevel = -1;

    public ComponentVillageCemetery() {}

    public ComponentVillageCemetery(StructureVillagePieces.Start startPiece, int type, Random rand,
            StructureBoundingBox bbox, int facing)
    {
        super(startPiece, type);
        this.coordBaseMode = EnumFacing.getHorizontal(facing);
        this.boundingBox = bbox;
    }

    public static ComponentVillageCemetery buildComponent(StructureVillagePieces.Start startPiece,
            List<StructureComponent> pieces, Random rand, int x, int y, int z, EnumFacing facing, int type)
    {
        StructureBoundingBox bbox = StructureBoundingBox.getComponentToAddBoundingBox(x, y, z, 0, 0, 0, 17, 5, 18, facing);
        if (!canVillageGoDeeper(bbox) || StructureComponent.findIntersecting(pieces, bbox) != null)
            return null;
        return new ComponentVillageCemetery(startPiece, type, rand, bbox, facing.getHorizontalIndex());
    }

    @Override
    public boolean addComponentParts(World world, Random rand, StructureBoundingBox sbb)
    {
        if (averageGroundLevel < 0)
        {
            averageGroundLevel = getAverageGroundLevel(world, sbb);
            if (averageGroundLevel < 0) return true;
        }
        boundingBox.offset(0, averageGroundLevel - boundingBox.minY - 1, 0);

        // Floor
        fillWithBlocks(world, sbb, 1, 1, 1, 15, 1, 16, Blocks.GRASS.getDefaultState(),
                Blocks.DIRT.getDefaultState(), false);

        // Walls — outer frame
        for (int x = 1; x <= 15; x++)
        {
            setBlockState(world, Blocks.COBBLESTONE.getDefaultState(), x, 2, 1, sbb);
            setBlockState(world, Blocks.COBBLESTONE.getDefaultState(), x, 2, 16, sbb);
        }
        for (int z = 1; z <= 16; z++)
        {
            setBlockState(world, Blocks.COBBLESTONE.getDefaultState(), 1, 2, z, sbb);
            setBlockState(world, Blocks.COBBLESTONE.getDefaultState(), 15, 2, z, sbb);
        }

        // Graves
        for (int i = 0; i < 3; i++)
        {
            int gx = 3 + i * 4;
            setBlockState(world, Blocks.COBBLESTONE_WALL.getDefaultState(), gx, 2, 4, sbb);
            setBlockState(world, Blocks.COBBLESTONE_WALL.getDefaultState(), gx, 2, 8, sbb);
            setBlockState(world, Blocks.COBBLESTONE_WALL.getDefaultState(), gx, 2, 12, sbb);
        }

        // Chest with body parts
        setBlockState(world, Blocks.CHEST.getDefaultState(), 8, 2, 8, sbb);
        TileEntityChest chest = (TileEntityChest) world.getTileEntity(getBlockPosAt(8, 2, 8, sbb));
        if (chest != null)
        {
            populateChest(chest, rand);
        }

        return true;
    }

    private BlockPos getBlockPosAt(int x, int y, int z, StructureBoundingBox sbb)
    {
        return new BlockPos(getXWithOffset(x, z), getYWithOffset(y), getZWithOffset(x, z));
    }

    private void populateChest(TileEntityChest chest, Random rand)
    {
        List<String> entityNames = new java.util.ArrayList<>(NecroEntityRegistry.registeredEntities.keySet());
        if (entityNames.isEmpty()) return;

        for (int i = 0; i < 4; i++)
        {
            String entityName = entityNames.get(rand.nextInt(entityNames.size()));
            NecroEntityBase entity = NecroEntityRegistry.registeredEntities.get(entityName);

            String[] partTypes = { "Head", "Torso", "Arm", "Legs" };
            for (String partType : partTypes)
            {
                ItemStack stack = ItemBodyPart.getItemStackFromName(entityName + " " + partType, 1);
                if (!stack.isEmpty())
                {
                    int slot = rand.nextInt(chest.getSizeInventory());
                    if (chest.getStackInSlot(slot).isEmpty())
                    {
                        chest.setInventorySlotContents(slot, stack);
                        break;
                    }
                }
            }
        }
    }
}
