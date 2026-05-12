package com.sirolf2009.necromancy.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/** Creative-only placeholder for a future mob capture cage pipeline (idea&nbsp;2 backlog). */
public class ItemMobCagePlaceholder extends Item {

    public ItemMobCagePlaceholder() {
        super(new Properties());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.necromancy.mob_cage.tooltip"));
    }
}
