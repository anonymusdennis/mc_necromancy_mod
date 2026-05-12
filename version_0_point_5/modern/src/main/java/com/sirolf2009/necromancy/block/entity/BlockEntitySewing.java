package com.sirolf2009.necromancy.block.entity;

import com.sirolf2009.necromancy.block.NecromancyBlocks;
import com.sirolf2009.necromancy.inventory.ContainerSewing;
import com.sirolf2009.necromancy.item.NecromancyItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Block entity for the Sewing Machine -- holds the persistent needle/string
 * "requirement" slots; the actual 4x4 craft grid lives in the
 * {@link com.sirolf2009.necromancy.inventory.ContainerSewing} on the client.
 *
 * <p>Direct port of {@code TileEntitySewing}.  Slot mapping:
 * <ul>
 *     <li>0 -> Bone Needle</li>
 *     <li>1 -> Vanilla String</li>
 * </ul>
 */
public class BlockEntitySewing extends BlockEntity implements Container, MenuProvider {

    public static final int SIZE = 2;
    public static final int SLOT_NEEDLE = 0;
    public static final int SLOT_STRING = 1;

    private final NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);

    public BlockEntitySewing(BlockPos pos, BlockState state) {
        super(NecromancyBlocks.SEWING_BE.get(), pos, state);
    }

    /** True iff slot 0 has a Bone Needle and slot 1 has String. */
    public boolean hasRequirements() {
        ItemStack needle = getItem(SLOT_NEEDLE);
        ItemStack string = getItem(SLOT_STRING);
        return !needle.isEmpty() && needle.is(NecromancyItems.BONE_NEEDLE.get())
            && !string.isEmpty() && string.is(Items.STRING);
    }

    /**
     * Consume one of each requirement.  Called by {@link com.sirolf2009.necromancy.inventory.SlotSewingResult}
     * when the player takes the result item from the output slot.
     */
    public void consumeRequirements() {
        removeItem(SLOT_NEEDLE, 1);
        removeItem(SLOT_STRING, 1);
    }

    // ---------------------------------------------------------------- Container
    @Override public int getContainerSize() { return SIZE; }
    @Override public boolean isEmpty() { for (ItemStack s : items) if (!s.isEmpty()) return false; return true; }
    @Override public ItemStack getItem(int s) { return items.get(s); }
    @Override public ItemStack removeItem(int s, int n) { ItemStack r = ContainerHelper.removeItem(items, s, n); setChanged(); return r; }
    @Override public ItemStack removeItemNoUpdate(int s) { return ContainerHelper.takeItem(items, s); }
    @Override public void setItem(int s, ItemStack stk) {
        items.set(s, stk);
        if (stk.getCount() > getMaxStackSize()) stk.setCount(getMaxStackSize());
        setChanged();
    }
    @Override public boolean stillValid(Player p) { return Container.stillValidBlockEntity(this, p); }
    @Override public void clearContent() { items.clear(); }

    @Override public Component getDisplayName() { return Component.translatable("container.necromancy.sewing_machine"); }
    @Override public AbstractContainerMenu createMenu(int id, Inventory inv, Player p) {
        return new ContainerSewing(id, inv, this);
    }

    // ---------------------------------------------------------------- Save/load
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider lookup) {
        super.loadAdditional(tag, lookup);
        items.clear();
        ContainerHelper.loadAllItems(tag, items, lookup);
    }
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider lookup) {
        super.saveAdditional(tag, lookup);
        ContainerHelper.saveAllItems(tag, items, lookup);
    }
}
