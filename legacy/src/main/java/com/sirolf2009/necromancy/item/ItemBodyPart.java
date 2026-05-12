package com.sirolf2009.necromancy.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import com.sirolf2009.necromancy.Necromancy;

import java.util.ArrayList;
import java.util.List;

public class ItemBodyPart extends Item
{
    public static final List<String> NECRO_ENTITIES = new ArrayList<>();

    public ItemBodyPart()
    {
        setHasSubtypes(true);
        setMaxDamage(0);
        setCreativeTab(Necromancy.tabNecromancyBodyParts);

        addParts("Cow", "Torso", "Head", "Arm", "Legs");
        addParts("Creeper", "Torso", "Legs");
        addParts("Enderman", "Head", "Torso", "Arm", "Legs");
        addParts("Pig", "Head", "Torso", "Arm", "Legs");
        addParts("Pigzombie", "Head", "Torso", "Arm", "Legs");
        addParts("Skeleton", "Torso", "Arm", "Legs");
        addParts("Spider", "Head", "Torso", "Legs");
        addParts("Zombie", "Torso", "Arm", "Legs");
        addParts("Chicken", "Head", "Torso", "Arm", "Legs");
        addParts("Villager", "Head", "Torso", "Arm", "Legs");
        addParts("Witch", "Head", "Torso", "Arm", "Legs");
        addParts("Squid", "Head", "Torso", "Legs");
        addParts("CaveSpider", "Head", "Torso", "Legs");
        addParts("Sheep", "Head", "Torso", "Arm", "Legs");
        addParts("IronGolem", "Head", "Torso", "Arm", "Legs");
        addParts("Wolf", "Head");
    }

    private void addParts(String entity, String... parts)
    {
        for (String part : parts)
            NECRO_ENTITIES.add(entity + " " + part);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {
        int meta = stack.getMetadata();
        if (meta >= 0 && meta < NECRO_ENTITIES.size()) return NECRO_ENTITIES.get(meta);
        return "Unknown Body Part";
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items)
    {
        if (isInCreativeTab(tab))
            for (int i = 0; i < NECRO_ENTITIES.size(); i++)
                items.add(new ItemStack(this, 1, i));
    }

    public static ItemStack getItemStackFromName(String name, int amount)
    {
        for (int i = 0; i < NECRO_ENTITIES.size(); i++)
            if (NECRO_ENTITIES.get(i).equalsIgnoreCase(name))
                return new ItemStack(RegistryNecromancyItems.bodyparts, amount, i);
        System.err.println("Necromancy: body part '" + name + "' not found in registry");
        return ItemStack.EMPTY;
    }
}
