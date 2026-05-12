package com.sirolf2009.necromancy.crafting;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

/**
 * 4x4 craft grid used by the Sewing Machine.
 *
 * <p>Vanilla's {@link net.minecraft.world.inventory.TransientCraftingContainer}
 * is hardcoded to 3x3, so we ship our own implementation that conforms to the
 * same {@link CraftingContainer} interface and exposes width/height as 4.
 */
public class SewingCraftMatrix implements Container, CraftingContainer {

    private final NonNullList<ItemStack> items;
    private final int width;
    private final int height;
    private final AbstractContainerMenu menu;

    public SewingCraftMatrix(AbstractContainerMenu menu, int width, int height) {
        this.menu   = menu;
        this.width  = width;
        this.height = height;
        this.items  = NonNullList.withSize(width * height, ItemStack.EMPTY);
    }

    @Override public int getContainerSize() { return items.size(); }
    @Override public int getMaxStackSize()  { return 64; }
    @Override public boolean isEmpty()      { for (ItemStack s : items) if (!s.isEmpty()) return false; return true; }
    @Override public ItemStack getItem(int s) { return items.get(s); }
    @Override public ItemStack removeItem(int s, int n) {
        ItemStack r = ContainerHelper.removeItem(items, s, n);
        if (!r.isEmpty()) menu.slotsChanged(this);
        return r;
    }
    @Override public ItemStack removeItemNoUpdate(int s) { return ContainerHelper.takeItem(items, s); }
    @Override public void setItem(int s, ItemStack stk) { items.set(s, stk); menu.slotsChanged(this); }
    @Override public void setChanged() {}
    @Override public boolean stillValid(Player p) { return true; }
    @Override public void clearContent() { items.clear(); }

    @Override public int getWidth()  { return width; }
    @Override public int getHeight() { return height; }
    @Override public java.util.List<ItemStack> getItems() { return items; }
    @Override public void fillStackedContents(net.minecraft.world.entity.player.StackedContents counter) {
        for (ItemStack s : items) counter.accountStack(s);
    }
}
