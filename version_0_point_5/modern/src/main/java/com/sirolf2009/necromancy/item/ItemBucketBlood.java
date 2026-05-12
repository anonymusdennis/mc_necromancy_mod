package com.sirolf2009.necromancy.item;

import com.sirolf2009.necromancy.fluid.NecromancyFluids;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;

/**
 * The "blood bucket" -- a vanilla {@link BucketItem} configured for our
 * {@code blood} {@link com.sirolf2009.necromancy.fluid.NecromancyFluids#BLOOD}
 * fluid.  Right-clicking a block places blood; right-clicking a blood source
 * picks it up again (handled automatically by the empty bucket).
 *
 * <p>Direct port of {@code ItemBucketBlood}.
 */
public class ItemBucketBlood extends BucketItem {
    public ItemBucketBlood() {
        super(NecromancyFluids.BLOOD_SOURCE.get(),
            new Item.Properties()
                .craftRemainder(net.minecraft.world.item.Items.BUCKET)
                .stacksTo(1));
    }
}
