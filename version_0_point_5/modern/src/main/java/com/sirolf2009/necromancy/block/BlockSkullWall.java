package com.sirolf2009.necromancy.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Thin wall-mounted skull decoration.
 *
 * <p>Direct port of {@code BlockSkullWall}.  In the original this was a
 * non-solid {@code Material.circuits} block that displayed a 3D skull glued
 * to a wall.  We preserve the slim shape and non-collision behaviour.
 */
public class BlockSkullWall extends HorizontalDirectionalBlock {

    public static final com.mojang.serialization.MapCodec<BlockSkullWall> CODEC = simpleCodec(BlockSkullWall::new);

    private static final VoxelShape SHAPE = Shapes.box(0.25, 0.25, 0.0, 0.75, 0.75, 0.5);

    public BlockSkullWall(Properties props) {
        super(props);
        registerDefaultState(getStateDefinition().any().setValue(FACING, net.minecraft.core.Direction.NORTH));
    }

    @Override
    protected com.mojang.serialization.MapCodec<? extends HorizontalDirectionalBlock> codec() { return CODEC; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    public VoxelShape getShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) { return SHAPE; }

    @Override
    public VoxelShape getCollisionShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) { return Shapes.empty(); }
}
