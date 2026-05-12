package com.sirolf2009.necromancy.inventory;

import com.sirolf2009.necromancy.block.NecromancyBlocks;
import com.sirolf2009.necromancy.block.entity.BlockEntityBodypartDev;
import com.sirolf2009.necromancy.gui.BodypartDevLayout;
import com.sirolf2009.necromancy.item.ItemBodyPart;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ContainerBodypartDev extends AbstractContainerMenu {

    private final BlockEntityBodypartDev dev;
    private final ContainerLevelAccess access;
    private final BlockPos devBlockPos;

    public ContainerBodypartDev(int id, Inventory playerInv, RegistryFriendlyByteBuf buf) {
        this(id, playerInv, lookup(playerInv, buf.readBlockPos()));
    }

    public ContainerBodypartDev(int id, Inventory playerInv, BlockEntityBodypartDev dev) {
        super(NecromancyMenus.BODYPART_DEV.get(), id);
        this.dev = dev;
        this.devBlockPos = dev.getBlockPos();
        this.access = ContainerLevelAccess.create(dev.getLevel(), devBlockPos);

        addSlot(new Slot(dev, BlockEntityBodypartDev.SLOT_PART, BodypartDevLayout.SLOT_PART_X, BodypartDevLayout.SLOT_PART_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof ItemBodyPart;
            }
        });

        final int invFirstRowY = BodypartDevLayout.PLAYER_INV_FIRST_ROW_Y;
        final int hotbarY = invFirstRowY + 18 * 3 + BodypartDevLayout.HOTBAR_EXTRA_GAP;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, invFirstRowY + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInv, col, 8 + col * 18, hotbarY));
        }
    }

    private static BlockEntityBodypartDev lookup(Inventory inv, BlockPos pos) {
        BlockEntity be = inv.player.level().getBlockEntity(pos);
        if (be instanceof BlockEntityBodypartDev d) return d;
        return new BlockEntityBodypartDev(pos, NecromancyBlocks.BODYPART_DEV.get().defaultBlockState());
    }

    public BlockEntityBodypartDev getDev() {
        return dev;
    }

    public BlockPos getDevBlockPos() {
        return devBlockPos;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, NecromancyBlocks.BODYPART_DEV.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index == 0) {
            if (!moveItemStackTo(stack, 1, 37, true)) return ItemStack.EMPTY;
        } else {
            if (!(stack.getItem() instanceof ItemBodyPart)) return ItemStack.EMPTY;
            if (!moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        if (stack.getCount() == copy.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return copy;
    }
}
