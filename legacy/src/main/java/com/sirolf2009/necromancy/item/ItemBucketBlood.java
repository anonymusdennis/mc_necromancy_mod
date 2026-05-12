package com.sirolf2009.necromancy.item;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBucket;
import com.sirolf2009.necromancy.Necromancy;

public class ItemBucketBlood extends ItemBucket
{
    public ItemBucketBlood(Block block)
    {
        super(block);
        setCreativeTab(Necromancy.tabNecromancy);
        setMaxStackSize(1);
    }
}
