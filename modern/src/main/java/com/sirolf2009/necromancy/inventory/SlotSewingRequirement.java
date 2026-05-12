package com.sirolf2009.necromancy.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

/**
 * Slot that only accepts items matching a predicate (used for the sewing
 * machine's needle and string slots).
 *
 * <p>Direct port of {@code SlotSewingRequirements}.
 */
public class SlotSewingRequirement extends Slot {
    private final Predicate<ItemStack> filter;

    public SlotSewingRequirement(Container container, int index, int x, int y, Predicate<ItemStack> filter) {
        super(container, index, x, y);
        this.filter = filter;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return filter.test(stack);
    }
}
