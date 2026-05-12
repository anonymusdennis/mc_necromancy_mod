package com.sirolf2009.necromancy.block.entity;

import com.sirolf2009.necromancy.block.NecromancyBlocks;
import com.sirolf2009.necromancy.inventory.ContainerOperationTable;
import net.minecraft.core.BlockPos;import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BlockEntityOperationTable extends BlockEntity implements MenuProvider {

    public BlockEntityOperationTable(BlockPos pos, BlockState state) {
        super(NecromancyBlocks.OPERATION_TABLE_BE.get(), pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.necromancy.operation_table");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new ContainerOperationTable(id, inv, worldPosition);
    }
}
