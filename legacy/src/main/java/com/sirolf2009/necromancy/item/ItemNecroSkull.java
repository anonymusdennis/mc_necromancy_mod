package com.sirolf2009.necromancy.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.common.registry.GameRegistry;

import com.sirolf2009.necromancy.Necromancy;
import com.sirolf2009.necroapi.NecroEntityRegistry;

import java.util.ArrayList;
import java.util.List;

public class ItemNecroSkull extends Item
{
    public static String[] skullTypes;

    public ItemNecroSkull()
    {
        setHasSubtypes(true);
        setMaxDamage(0);
        setCreativeTab(Necromancy.tabNecromancy);
    }

    public static void initSkulls()
    {
        List<String> names = new ArrayList<>(NecroEntityRegistry.registeredEntities.keySet());
        skullTypes = names.toArray(new String[0]);
        Item skullItem = new ItemNecroSkull().setUnlocalizedName("NecroSkull").setRegistryName("necromancy", "necro_skull");
        GameRegistry.findRegistry(Item.class).register(skullItem);
        RegistryNecromancyItems.isaacsHead = skullItem;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {
        int meta = stack.getMetadata();
        if (skullTypes != null && meta >= 0 && meta < skullTypes.length)
            return skullTypes[meta] + " Skull";
        return "Skull";
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items)
    {
        if (isInCreativeTab(tab) && skullTypes != null)
            for (int i = 0; i < skullTypes.length; i++)
                items.add(new ItemStack(this, 1, i));
    }
}
