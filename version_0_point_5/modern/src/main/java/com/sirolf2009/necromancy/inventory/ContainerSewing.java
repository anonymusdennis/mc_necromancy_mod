package com.sirolf2009.necromancy.inventory;

import com.sirolf2009.necromancy.block.NecromancyBlocks;
import com.sirolf2009.necromancy.block.entity.BlockEntitySewing;
import com.sirolf2009.necromancy.crafting.SewingCraftMatrix;
import com.sirolf2009.necromancy.crafting.SewingRecipe;
import com.sirolf2009.necromancy.crafting.CraftingManagerSewing;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Container/menu for the Sewing Machine.  Layout (matches legacy 1:1):
 * <pre>
 *  0..15  -> 4x4 craft grid
 *     16  -> needle slot   (BE.slot 0)
 *     17  -> string slot   (BE.slot 1)
 *     18  -> result slot   (own ResultContainer)
 *  19..45 -> player main inventory
 *  46..54 -> hotbar
 * </pre>
 */
public class ContainerSewing extends AbstractContainerMenu {

    private final BlockEntitySewing sewing;
    private final ContainerLevelAccess access;
    private final SewingCraftMatrix craftMatrix;
    private final ResultContainer craftResult = new ResultContainer();
    private final Player player;

    public ContainerSewing(int id, Inventory playerInv, RegistryFriendlyByteBuf buf) {
        this(id, playerInv, lookupSewing(playerInv, buf.readBlockPos()));
    }

    public ContainerSewing(int id, Inventory playerInv, BlockEntitySewing sewing) {
        super(NecromancyMenus.SEWING.get(), id);
        this.sewing      = sewing;
        this.player      = playerInv.player;
        this.access      = ContainerLevelAccess.create(sewing.getLevel(), sewing.getBlockPos());
        this.craftMatrix = new SewingCraftMatrix(this, 4, 4);

        // 4x4 craft grid (slots 0..15)
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                addSlot(new Slot(craftMatrix, col + row * 4, 8 + col * 18, 8 + row * 18));
            }
        }
        // Requirement slots: needle (95, 17), string (95, 54)
        addSlot(new SlotSewingRequirement(sewing, BlockEntitySewing.SLOT_NEEDLE, 95, 17,
            stk -> stk.is(com.sirolf2009.necromancy.item.NecromancyItems.BONE_NEEDLE.get())));
        addSlot(new SlotSewingRequirement(sewing, BlockEntitySewing.SLOT_STRING, 95, 54,
            stk -> stk.is(net.minecraft.world.item.Items.STRING)));
        // Result (145, 35)
        addSlot(new SlotSewingResult(player, this, craftMatrix, craftResult, 0, 145, 35));

        // Player inventory
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInv, col, 8 + col * 18, 142));
        }
        slotsChanged(craftMatrix);
    }

    private static BlockEntitySewing lookupSewing(Inventory inv, BlockPos pos) {
        BlockEntity be = inv.player.level().getBlockEntity(pos);
        if (be instanceof BlockEntitySewing s) return s;
        return new BlockEntitySewing(pos, NecromancyBlocks.SEWING_MACHINE.get().defaultBlockState());
    }

    public BlockEntitySewing getSewing() { return sewing; }
    public SewingCraftMatrix getCraftMatrix() { return craftMatrix; }
    public ResultContainer getCraftResult() { return craftResult; }
    public Player getPlayer() { return player; }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, NecromancyBlocks.SEWING_MACHINE.get());
    }

    @Override
    public void slotsChanged(Container container) {
        if (sewing.hasRequirements()) {
            craftResult.setItem(0, CraftingManagerSewing.findMatching(craftMatrix, player.level()));
        } else {
            craftResult.setItem(0, ItemStack.EMPTY);
        }
        broadcastChanges();
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide) {
            // Drop everything in the craft matrix back to the player.
            for (int i = 0; i < craftMatrix.getContainerSize(); i++) {
                ItemStack stk = craftMatrix.removeItemNoUpdate(i);
                if (!stk.isEmpty()) {
                    player.getInventory().placeItemBackInInventory(stk);
                }
            }
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy  = stack.copy();

        // Layout indices (consistent with addSlot order above):
        //   0..15   craft grid
        //   16,17   needle/string
        //   18      result
        //   19..45  player
        if (index == 18) {
            // Result -> player
            if (!moveItemStackTo(stack, 19, 55, true)) return ItemStack.EMPTY;
            slot.onQuickCraft(stack, copy);
        } else if (index >= 0 && index < 16) {
            if (!moveItemStackTo(stack, 19, 55, false)) return ItemStack.EMPTY;
        } else if (index == 16 || index == 17) {
            if (!moveItemStackTo(stack, 19, 55, false)) return ItemStack.EMPTY;
        } else {
            // From player -> machine: try to slot needle/string smartly
            if (stack.is(com.sirolf2009.necromancy.item.NecromancyItems.BONE_NEEDLE.get())) {
                if (!moveItemStackTo(stack, 16, 17, false)) return ItemStack.EMPTY;
            } else if (stack.is(net.minecraft.world.item.Items.STRING)) {
                if (!moveItemStackTo(stack, 17, 18, false)) return ItemStack.EMPTY;
            } else {
                if (!moveItemStackTo(stack, 0, 16, false)) return ItemStack.EMPTY;
            }
        }
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        if (stack.getCount() == copy.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return copy;
    }
}
