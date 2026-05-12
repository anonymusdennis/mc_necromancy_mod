package com.sirolf2009.necromancy.client.integration.jei;

import net.minecraft.world.item.ItemStack;

/** One JEI “handbook” entry keyed by the item shown in the slot (lookup uses that stack). */
public record GuideJeiRecipe(ItemStack stack) {
    public GuideJeiRecipe {
        stack = stack.copy();
    }
}
