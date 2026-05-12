package com.sirolf2009.necromancy.block;

import com.sirolf2009.necromancy.block.entity.BlockEntitySewing;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Sewing Machine block.  Right-clicking opens its 4x4 craft GUI.  The block is
 * a {@link BaseEntityBlock} so its {@link BlockEntitySewing} can hold the
 * needle/string requirement slots and the cached recipe output.
 *
 * <p>Direct port of {@code BlockSewing}.
 */
public class BlockSewing extends BaseEntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final com.mojang.serialization.MapCodec<BlockSewing> CODEC = simpleCodec(BlockSewing::new);
    private static final VoxelShape SHAPE = Shapes.box(0, 0, 0, 1, 0.95, 1);

    public BlockSewing(Properties props) {
        super(props);
        registerDefaultState(getStateDefinition().any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected com.mojang.serialization.MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> b) {
        b.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override public RenderShape getRenderShape(BlockState s) { return RenderShape.MODEL; }

    @Override
    public VoxelShape getShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) { return SHAPE; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntitySewing(pos, state);
    }

    @Override
    protected net.minecraft.world.InteractionResult useWithoutItem(BlockState s, Level level, BlockPos pos,
            Player player, BlockHitResult hit) {
        if (level.isClientSide) return net.minecraft.world.InteractionResult.SUCCESS;
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof MenuProvider mp) {
            player.openMenu(mp, buf -> buf.writeBlockPos(pos));
        }
        return net.minecraft.world.InteractionResult.CONSUME;
    }
}
