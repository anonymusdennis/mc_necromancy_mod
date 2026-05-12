package com.sirolf2009.necromancy.item;



import com.sirolf2009.necromancy.api.BodyPartLocation;

import com.sirolf2009.necromancy.api.NecroEntityRegistry;

import com.sirolf2009.necromancy.api.feature.PartFeature;

import com.sirolf2009.necromancy.bodypart.BodyPartConfigGate;

import com.sirolf2009.necromancy.bodypart.BodyPartItemIds;
import net.minecraft.network.chat.Component;

import net.minecraft.world.InteractionHand;

import net.minecraft.world.InteractionResultHolder;

import net.minecraft.world.entity.player.Player;

import net.minecraft.world.item.Item;

import net.minecraft.world.item.ItemStack;

import net.minecraft.world.item.TooltipFlag;

import net.minecraft.world.level.Level;



import java.util.List;



/**

 * One physical "body part" in an inventory slot.

 *

 * <p>The legacy mod packed all 54 body-parts into a single multi-meta

 * {@code ItemBodyPart} keyed by damage value.  In 1.21.1 there is no metadata

 * any more, so we register one {@link Item} instance per (mob, location) pair

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

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {

        ItemStack stack = player.getItemInHand(hand);

        if (!BodyPartConfigGate.allows(stack)) {

            if (!level.isClientSide()) {

                var rl = BodyPartItemIds.partId(stack.getItem());
                String idStr = rl != null ? rl.toString() : stack.getDescriptionId();

                var msg = switch (BodyPartConfigGate.reason(stack)) {
                    case NOT_VALIDATED -> Component.translatable("message.necromancy.bodypart.not_validated", idStr);
                    default -> Component.translatable("message.necromancy.bodypart.unconfigured", idStr);
                };
                player.displayClientMessage(msg, true);

            }

            return InteractionResultHolder.fail(stack);

        }

        return InteractionResultHolder.pass(stack);

    }



    @Override

    public void appendHoverText(ItemStack stack, Item.TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {

        tooltip.add(Component.translatable("item.necromancy.bodypart.tooltip"));

        if (!BodyPartConfigGate.allows(stack)) {
            var rl = BodyPartItemIds.partId(stack.getItem());
            String idStr = rl != null ? rl.toString() : stack.getDescriptionId();
            var tip = switch (BodyPartConfigGate.reason(stack)) {
                case NOT_VALIDATED -> Component.translatable("item.necromancy.bodypart.not_validated_tooltip", idStr);
                default -> Component.translatable("item.necromancy.bodypart.unconfigured", idStr);
            };
            tooltip.add(tip);

        }

        var adapter = NecroEntityRegistry.get(mobName);

        if (adapter != null) {

            for (PartFeature feature : adapter.features(location)) {

                feature.appendTooltip(location, tooltip);

            }

        }

    }

}

