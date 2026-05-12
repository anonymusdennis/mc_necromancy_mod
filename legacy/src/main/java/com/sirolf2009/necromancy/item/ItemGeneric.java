package com.sirolf2009.necromancy.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

import com.sirolf2009.necromancy.Necromancy;

import javax.annotation.Nullable;
import java.util.List;

public class ItemGeneric extends Item
{
    public static final String[] NAMES = { "Bone Needle", "Soul in a Jar", "Jar of Blood", "Brain on a Stick" };

    public ItemGeneric()
    {
        setHasSubtypes(true);
        setMaxDamage(0);
        setCreativeTab(Necromancy.tabNecromancy);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity)
    {
        if (stack.getMetadata() == 0 && player.inventory.consumeInventoryItem(Items.GLASS_BOTTLE))
        {
            stack.shrink(1);
            ItemStack bloodJar = new ItemStack(RegistryNecromancyItems.genericItems, 1, 2);
            if (!player.inventory.addItemStackToInventory(bloodJar))
            {
                player.entityDropItem(bloodJar, 0f);
            }
        }
        return false;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {
        int meta = stack.getMetadata();
        if (meta >= 0 && meta < NAMES.length)
            return NAMES[meta];
        return "Unknown Necromancy Item";
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items)
    {
        if (isInCreativeTab(tab))
            for (int i = 0; i < NAMES.length; i++)
                items.add(new ItemStack(this, 1, i));
    }

    public static ItemStack getItemStackFromName(String name)
    {
        for (int i = 0; i < NAMES.length; i++)
            if (NAMES[i].equalsIgnoreCase(name))
                return new ItemStack(RegistryNecromancyItems.genericItems, 1, i);
        return ItemStack.EMPTY;
    }

    public static ItemStack getItemStackFromName(String name, int amount)
    {
        for (int i = 0; i < NAMES.length; i++)
            if (NAMES[i].equalsIgnoreCase(name))
                return new ItemStack(RegistryNecromancyItems.genericItems, amount, i);
        return ItemStack.EMPTY;
    }
}
