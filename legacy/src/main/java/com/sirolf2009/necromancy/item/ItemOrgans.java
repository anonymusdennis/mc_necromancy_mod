package com.sirolf2009.necromancy.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.NonNullList;

import com.sirolf2009.necromancy.Necromancy;

public class ItemOrgans extends ItemFood
{
    public static final String[] NAMES = { "Brains", "Heart", "Muscle", "Lungs", "Skin" };

    public ItemOrgans()
    {
        super(2, true);
        setAlwaysEdible();
        setHasSubtypes(true);
        setMaxDamage(0);
        setCreativeTab(Necromancy.tabNecromancy);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {
        int meta = stack.getMetadata();
        if (meta >= 0 && meta < NAMES.length) return NAMES[meta];
        return "Invalid Organ";
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items)
    {
        if (isInCreativeTab(tab))
            for (int i = 0; i < NAMES.length; i++)
                items.add(new ItemStack(this, 1, i));
    }
}
