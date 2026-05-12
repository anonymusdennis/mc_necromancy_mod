package com.sirolf2009.necromancy.item;

import net.minecraft.world.item.Item;

/**
 * The Bone Needle -- crafted from a single ink sac.  Used as the catalyst in
 * every sewing recipe and is consumed when the recipe is sewn.
 *
 * <p>Direct port of the legacy {@code ItemGeneric "Bone Needle"} sub-item.
 */
public class ItemBoneNeedle extends Item {
    public ItemBoneNeedle() {
        super(new Item.Properties().stacksTo(64));
    }
}
