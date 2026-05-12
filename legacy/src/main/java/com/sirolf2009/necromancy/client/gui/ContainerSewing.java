package com.sirolf2009.necromancy.client.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import com.sirolf2009.necromancy.tileentity.TileEntitySewing;

public class ContainerSewing extends Container
{
    private final TileEntitySewing sewing;

    public ContainerSewing(InventoryPlayer playerInventory, TileEntitySewing sewing)
    {
        this.sewing = sewing;
        for (int i = 0; i < sewing.getSizeInventory(); i++)
            addSlotToContainer(new Slot(sewing, i, 8 + i * 20, 8));

        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                addSlotToContainer(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
        for (int col = 0; col < 9; col++)
            addSlotToContainer(new Slot(playerInventory, col, 8 + col * 18, 142));
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) { return sewing.isUsableByPlayer(player); }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) { return ItemStack.EMPTY; }
}
