package com.sirolf2009.necromancy.client.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import com.sirolf2009.necromancy.tileentity.TileEntityAltar;

public class ContainerAltar extends Container
{
    private final TileEntityAltar altar;

    public ContainerAltar(InventoryPlayer playerInventory, TileEntityAltar altar)
    {
        this.altar = altar;
        // Slots: 0=blood, 1=soul, 2=head, 3=torso, 4=legs, 5=armRight, 6=armLeft
        addSlotToContainer(new Slot(altar, 0, 8, 8));
        addSlotToContainer(new Slot(altar, 1, 8, 28));
        addSlotToContainer(new Slot(altar, 2, 80, 8));
        addSlotToContainer(new Slot(altar, 3, 80, 28));
        addSlotToContainer(new Slot(altar, 4, 80, 48));
        addSlotToContainer(new Slot(altar, 5, 60, 28));
        addSlotToContainer(new Slot(altar, 6, 100, 28));

        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                addSlotToContainer(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
        for (int col = 0; col < 9; col++)
            addSlotToContainer(new Slot(playerInventory, col, 8 + col * 18, 142));
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) { return altar.isUsableByPlayer(player); }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index)
    {
        return ItemStack.EMPTY;
    }
}
