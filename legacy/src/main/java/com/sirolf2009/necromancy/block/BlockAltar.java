package com.sirolf2009.necromancy.block;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import com.sirolf2009.necromancy.Necromancy;
import com.sirolf2009.necromancy.tileentity.TileEntityAltar;

import java.util.Random;

public class BlockAltar extends BlockContainer
{
    public static final int GUI_ID = 0;

    public BlockAltar()
    {
        super(Material.ROCK);
        setCreativeTab(Necromancy.tabNecromancy);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
            EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        TileEntityAltar te = (TileEntityAltar) world.getTileEntity(pos);
        if (te == null) return false;
        if (player.isSneaking() && (te.canSpawn() || player.isCreative()))
        {
            te.spawn(player);
            return true;
        }
        else
        {
            player.openGui(Necromancy.instance, GUI_ID, world, pos.getX(), pos.getY(), pos.getZ());
            return true;
        }
    }

    @Override
    public void onBlockDestroyedByPlayer(World world, BlockPos pos, IBlockState state)
    {
        TileEntityAltar te = (TileEntityAltar) world.getTileEntity(pos);
        Random rand = new Random();
        if (te != null)
        {
            for (int i = 0; i < te.getSizeInventory(); i++)
            {
                ItemStack stack = te.getStackInSlot(i);
                if (!stack.isEmpty())
                {
                    float fx = rand.nextFloat() * 0.8F + 0.1F;
                    float fy = rand.nextFloat() * 0.8F + 0.1F;
                    float fz = rand.nextFloat() * 0.8F + 0.1F;
                    while (stack.getCount() > 0)
                    {
                        int count = Math.min(rand.nextInt(21) + 10, stack.getCount());
                        stack.shrink(count);
                        EntityItem drop = new EntityItem(world, pos.getX() + fx, pos.getY() + fy,
                                pos.getZ() + fz, new ItemStack(stack.getItem(), count, stack.getMetadata()));
                        float speed = 0.05F;
                        drop.motionX = rand.nextGaussian() * speed;
                        drop.motionY = rand.nextGaussian() * speed + 0.2D;
                        drop.motionZ = rand.nextGaussian() * speed;
                        world.spawnEntity(drop);
                    }
                }
            }
        }
        super.onBlockDestroyedByPlayer(world, pos, state);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta)
    {
        return new TileEntityAltar();
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
