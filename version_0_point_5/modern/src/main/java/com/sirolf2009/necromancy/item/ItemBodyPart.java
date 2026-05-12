package com.sirolf2009.necromancy.item;

import com.sirolf2009.necromancy.api.BodyPartLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * One physical "body part" in an inventory slot.
 *
 * <p>The legacy mod packed all 54 body-parts into a single multi-meta
 * {@code ItemBodyPart} keyed by damage value.  In 1.21.1 there is no metadata
 * any more, so we register one {@code Item} instance per (mob, location) pair
 * and let the {@link com.sirolf2009.necromancy.item.NecromancyItems} registry
 * track them.  The behaviour stays identical: the part holds the mob name and
 * the limb location, which is what the sewing manager and the altar look up.
 */
public class ItemBodyPart extends Item {

    private final String mobName;
    private final BodyPartLocation location;

    public ItemBodyPart(String mobName, BodyPartLocation location) {
        super(new Item.Properties().stacksTo(4));
        this.mobName  = mobName;
        this.location = location;
    }

    public String getMobName() { return mobName; }

    public BodyPartLocation getLocation() { return location; }

    /** "Cow Head", "Skeleton Arm", "Spider Legs", … as in the legacy item. */
    public String getPartKey() { return mobName + " " + location.partKey(); }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.necromancy.bodypart.tooltip"));
    }
}
