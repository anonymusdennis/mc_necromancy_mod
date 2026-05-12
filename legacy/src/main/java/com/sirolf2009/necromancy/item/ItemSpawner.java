package com.sirolf2009.necromancy.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import com.sirolf2009.necromancy.Necromancy;

public class ItemSpawner extends Item
{
    private static final String[] NAMES = { "Isaac's Soul Heart" };

    public ItemSpawner()
    {
        setHasSubtypes(true);
        setMaxDamage(0);
        setCreativeTab(Necromancy.tabNecromancy);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {
        int meta = stack.getMetadata();
        if (meta >= 0 && meta < NAMES.length) return NAMES[meta];
        return "Unknown Spawner";
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items)
    {
        if (isInCreativeTab(tab))
            for (int i = 0; i < NAMES.length; i++)
                items.add(new ItemStack(this, 1, i));
    }
}
