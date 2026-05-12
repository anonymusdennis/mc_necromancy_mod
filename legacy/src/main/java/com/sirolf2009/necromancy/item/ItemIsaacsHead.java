package com.sirolf2009.necromancy.item;

import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import com.sirolf2009.necromancy.Necromancy;

public class ItemIsaacsHead extends ItemArmor
{
    public ItemIsaacsHead(ArmorMaterial material, int renderIndex, int armorType)
    {
        super(material, renderIndex, net.minecraft.inventory.EntityEquipmentSlot.HEAD);
        setCreativeTab(Necromancy.tabNecromancy);
        setMaxStackSize(1);
    }
}
