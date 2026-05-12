package com.sirolf2009.necromancy.inventory;

import com.sirolf2009.necromancy.block.NecromancyBlocks;
import com.sirolf2009.necromancy.block.entity.BlockEntityOperationTable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Operation table container menu — Phase&nbsp;F surgical tooling shell.
 *
 * <p>Networking backlog (plan F03): when graft payloads attach to {@link net.minecraft.world.inventory.MenuType}
 * custom data, mirror slot deltas into a byte ledger on {@link BlockEntityOperationTable} for rollback/replay. Slots are
 * currently ghost placeholders — {@link Slot#mayPlace} / {@link Slot#mayPickup} reject interaction so no payloads ship yet.
 */
public class ContainerOperationTable extends AbstractContainerMenu {

    private final ContainerLevelAccess access;

    public ContainerOperationTable(int id, Inventory playerInv, RegistryFriendlyByteBuf buf) {
        this(id, playerInv, buf.readBlockPos());
    }

    public ContainerOperationTable(int id, Inventory playerInv, BlockPos pos) {
        super(NecromancyMenus.OPERATION_TABLE.get(), id);
        BlockEntity be = playerInv.player.level().getBlockEntity(pos);
        if (!(be instanceof BlockEntityOperationTable table)) {
            throw new IllegalStateException("Missing operation table block entity at " + pos);
        }
        this.access = ContainerLevelAccess.create(table.getLevel(), table.getBlockPos());

        net.minecraft.world.SimpleContainer ghost = new net.minecraft.world.SimpleContainer(9);
        for (int i = 0; i < 9; i++) {
            int fi = i;
            addSlot(new Slot(ghost, fi, 30 + (fi % 3) * 18, 17 + (fi / 3) * 18) {
                @Override public boolean mayPlace(ItemStack stack) {
                    return false;
                }

                @Override public boolean mayPickup(Player p) {
                    return false;
                }
            });
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInv, col, 8 + col * 18, 142));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, NecromancyBlocks.OPERATION_TABLE.get());
    }
}
