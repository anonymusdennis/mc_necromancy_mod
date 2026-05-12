package com.sirolf2009.necromancy.client.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

import com.sirolf2009.necromancy.block.BlockAltar;
import com.sirolf2009.necromancy.block.BlockSewing;
import com.sirolf2009.necromancy.tileentity.TileEntityAltar;
import com.sirolf2009.necromancy.tileentity.TileEntitySewing;

import net.minecraft.util.math.BlockPos;

public class GuiHandler implements IGuiHandler
{
    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z)
    {
        BlockPos pos = new BlockPos(x, y, z);
        if (id == BlockAltar.GUI_ID)
            return new ContainerAltar(player.inventory, (TileEntityAltar) world.getTileEntity(pos));
        if (id == BlockSewing.GUI_ID)
            return new ContainerSewing(player.inventory, (TileEntitySewing) world.getTileEntity(pos));
        return null;
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z)
    {
        BlockPos pos = new BlockPos(x, y, z);
        if (id == BlockAltar.GUI_ID)
            return new GuiAltar(player.inventory, (TileEntityAltar) world.getTileEntity(pos));
        if (id == BlockSewing.GUI_ID)
            return new GuiSewing(player.inventory, (TileEntitySewing) world.getTileEntity(pos));
        return null;
    }
}
