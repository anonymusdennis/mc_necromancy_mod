package com.sirolf2009.necromancy.item;

import net.minecraft.world.item.Item;

/**
 * Jar of Blood -- placed in the altar's slot 0; consumed by every summon ritual.
 *
 * <p>Awarded by left-clicking a glass-bottle while wielding it, similar to the
 * legacy mod, when triggered against a freshly killed mob.
 */
public class ItemJarOfBlood extends Item {
    public ItemJarOfBlood() {
        super(new Item.Properties().stacksTo(16));
    }
}
