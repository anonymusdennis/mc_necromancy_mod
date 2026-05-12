package com.sirolf2009.necromancy.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

public class TileEntitySewing extends TileEntity implements IInventory
{
    private ItemStack[] inventory = new ItemStack[6];

    public TileEntitySewing()
    {
        for (int i = 0; i < inventory.length; i++)
            inventory[i] = ItemStack.EMPTY;
    }

    @Override
    public int getSizeInventory() { return inventory.length; }

    @Override
    public boolean isEmpty()
    {
        for (ItemStack s : inventory)
            if (!s.isEmpty()) return false;
        return true;
    }

    @Override
    public ItemStack getStackInSlot(int index) { return inventory[index]; }

    @Override
    public ItemStack decrStackSize(int index, int count)
    {
        if (inventory[index].isEmpty()) return ItemStack.EMPTY;
        if (inventory[index].getCount() <= count)
        {
            ItemStack old = inventory[index];
            inventory[index] = ItemStack.EMPTY;
            return old;
        }
        return inventory[index].splitStack(count);
    }

    @Override
    public ItemStack removeStackFromSlot(int index)
    {
        ItemStack old = inventory[index];
        inventory[index] = ItemStack.EMPTY;
        return old;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack)
    {
        inventory[index] = stack == null ? ItemStack.EMPTY : stack;
        if (!inventory[index].isEmpty() && inventory[index].getCount() > getInventoryStackLimit())
            inventory[index].setCount(getInventoryStackLimit());
    }

    @Override
    public String getName() { return "TileEntitySewing"; }

    @Override
    public boolean hasCustomName() { return false; }

    @Override
    public ITextComponent getDisplayName() { return new TextComponentString(getName()); }

    @Override
    public int getInventoryStackLimit() { return 64; }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player)
    {
        return world.getTileEntity(pos) == this
                && player.getDistanceSq(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) < 64;
    }

    @Override
    public void openInventory(EntityPlayer player) {}

    @Override
    public void closeInventory(EntityPlayer player) {}

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) { return false; }

    @Override
    public int getField(int id) { return 0; }

    @Override
    public void setField(int id, int value) {}

    @Override
    public int getFieldCount() { return 0; }

    @Override
    public void clear()
    {
        for (int i = 0; i < inventory.length; i++)
            inventory[i] = ItemStack.EMPTY;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        NBTTagList list = compound.getTagList("Inventory", 10);
        inventory = new ItemStack[getSizeInventory()];
        for (int i = 0; i < inventory.length; i++) inventory[i] = ItemStack.EMPTY;
        for (int i = 0; i < list.tagCount(); i++)
        {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            byte slot = tag.getByte("Slot");
            if (slot >= 0 && slot < inventory.length)
                inventory[slot] = new ItemStack(tag);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < inventory.length; i++)
        {
            if (!inventory[i].isEmpty())
            {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setByte("Slot", (byte) i);
                inventory[i].writeToNBT(tag);
                list.appendTag(tag);
            }
        }
        compound.setTag("Inventory", list);
        return compound;
    }
}
