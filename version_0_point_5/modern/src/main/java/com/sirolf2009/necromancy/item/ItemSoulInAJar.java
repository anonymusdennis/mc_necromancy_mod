package com.sirolf2009.necromancy.item;

import net.minecraft.world.item.Item;

/**
 * Soul in a Jar -- the binding catalyst placed in the altar's slot 1.
 *
 * <p>Awarded to the player when an enemy is finished off with a scythe while
 * the player is holding a glass bottle in their inventory.
 */
public class ItemSoulInAJar extends Item {
    public ItemSoulInAJar() {
        super(new Item.Properties().stacksTo(16));
    }
}
