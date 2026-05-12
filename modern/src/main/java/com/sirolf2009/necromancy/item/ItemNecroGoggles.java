package com.sirolf2009.necromancy.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/** Leather-tier helmet that unlocks subtle client-side minion AI overlays when worn. */
public final class ItemNecroGoggles extends ArmorItem {

    public ItemNecroGoggles() {
        super(ArmorMaterials.LEATHER, ArmorItem.Type.HELMET,
            new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(15)));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.necromancy.necro_goggles.tooltip"));
    }
}
