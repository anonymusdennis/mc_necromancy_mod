package com.sirolf2009.necromancy.craftingmanager;

import net.minecraft.item.ItemStack;

import com.sirolf2009.necroapi.NecroEntityBase;
import com.sirolf2009.necroapi.NecroEntityRegistry;
import com.sirolf2009.necromancy.item.ItemBodyPart;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class CraftingManagerSewing
{
    public CraftingManagerSewing()
    {
        registerSewingRecipes();
    }

    private void registerSewingRecipes()
    {
        for (NecroEntityBase mob : NecroEntityRegistry.registeredEntities.values())
        {
            if (mob.headRecipe != null)
                registerPartRecipe(mob.mobName + " Head", mob.headRecipe);
            if (mob.torsoRecipe != null)
                registerPartRecipe(mob.mobName + " Torso", mob.torsoRecipe);
            if (mob.armRecipe != null)
                registerPartRecipe(mob.mobName + " Arm", mob.armRecipe);
            if (mob.legRecipe != null)
                registerPartRecipe(mob.mobName + " Legs", mob.legRecipe);
        }
    }

    private void registerPartRecipe(String partName, Object[] recipe)
    {
        ItemStack result = ItemBodyPart.getItemStackFromName(partName, 1);
        if (!result.isEmpty())
        {
            GameRegistry.addShapedRecipe(new net.minecraft.util.ResourceLocation("necromancy", partName.replace(" ", "_").toLowerCase()),
                    null, result, recipe);
        }
    }
}
