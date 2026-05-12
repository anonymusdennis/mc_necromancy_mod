package com.sirolf2009.necromancy.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

import com.sirolf2009.necromancy.item.ItemGeneric;
import com.sirolf2009.necromancy.tileentity.TileEntityAltar;
import com.sirolf2009.necromancy.tileentity.TileEntitySewing;
import com.sirolf2009.necromancy.tileentity.TileEntitySkullWall;

public class RegistryBlocksNecromancy
{
    public static Block altar;
    public static Block altarBlock;
    public static Block sewing;
    public static Block blood;
    public static Block skullWall;

    public static Fluid fluidBlood;

    public static void initBlocks()
    {
        altar = new BlockAltar().setHardness(4).setTranslationKey("summoning_altar").setRegistryName("necromancy", "summoning_altar");
        GameRegistry.findRegistry(Block.class).register(altar);
        GameRegistry.registerTileEntity(TileEntityAltar.class, "necromancy:summoning_altar");

        altarBlock = new BlockAltarBlock().setHardness(4).setTranslationKey("altar_block").setRegistryName("necromancy", "altar_block");
        GameRegistry.findRegistry(Block.class).register(altarBlock);

        sewing = new BlockSewing(Material.IRON).setHardness(4).setTranslationKey("sewing_machine").setRegistryName("necromancy", "sewing_machine");
        GameRegistry.findRegistry(Block.class).register(sewing);
        GameRegistry.registerTileEntity(TileEntitySewing.class, "necromancy:sewing_machine");

        fluidBlood = FluidRegistry.getFluid("blood");
        if (fluidBlood == null)
        {
            fluidBlood = new Fluid("blood", null, null);
            FluidRegistry.registerFluid(fluidBlood);
            blood = new BlockBlood(fluidBlood);
            blood.setTranslationKey("flowing_blood").setRegistryName("necromancy", "flowing_blood");
            fluidBlood.setBlock(blood);
            GameRegistry.findRegistry(Block.class).register(blood);
        }
        else
        {
            blood = fluidBlood.getBlock();
        }

        skullWall = new BlockSkullWall().setTranslationKey("skull_wall").setRegistryName("necromancy", "skull_wall");
        GameRegistry.findRegistry(Block.class).register(skullWall);
        GameRegistry.registerTileEntity(TileEntitySkullWall.class, "necromancy:skull_wall");
    }

    public static void initRecipes()
    {
        GameRegistry.addRecipe(new ItemStack(RegistryBlocksNecromancy.sewing, 1),
                new Object[] { "III", "ISB", "III",
                        'I', Items.IRON_INGOT,
                        'S', Items.STRING,
                        'B', ItemGeneric.getItemStackFromName("Bone Needle") });
    }
}
