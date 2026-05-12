package com.sirolf2009.necromancy.creativetab;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

import com.sirolf2009.necromancy.item.RegistryNecromancyItems;

public class CreativeTabNecro extends CreativeTabs
{
    private final int iconType;

    public CreativeTabNecro(String label, int iconType)
    {
        super(label);
        this.iconType = iconType;
    }

    @Override
    public ItemStack getTabIconItem()
    {
        if (iconType == 2 && RegistryNecromancyItems.bodyparts != null)
            return new ItemStack(RegistryNecromancyItems.bodyparts);
        if (RegistryNecromancyItems.necronomicon != null)
            return new ItemStack(RegistryNecromancyItems.necronomicon);
        return new ItemStack(net.minecraft.init.Items.BOOK);
    }
}
