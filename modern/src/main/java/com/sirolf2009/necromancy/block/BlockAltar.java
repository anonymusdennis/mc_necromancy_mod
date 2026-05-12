package com.sirolf2009.necromancy.block;

import com.sirolf2009.necromancy.block.entity.BlockEntityAltar;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Summoning Altar block -- the centrepiece of the mod.  Right-click opens its
 * GUI; shift-right-click triggers the summon ritual when the recipe is valid.
 *
 * <p>Direct port of {@code BlockAltar}.  We keep the same drop behaviour
 * (placed altar destroys back into 1 plank + 2 cobble in the four cardinal
 * orientations, depending on the block's HORIZONTAL_FACING).
 */
public class BlockAltar extends BaseEntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final com.mojang.serialization.MapCodec<BlockAltar> CODEC = simpleCodec(p -> new BlockAltar(p));

    private static final VoxelShape SHAPE = Shapes.box(0, 0, 0, 1, 0.85, 1);

    public BlockAltar(Properties props) {
        super(props);
        registerDefaultState(getStateDefinition().any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected com.mojang.serialization.MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    /**
     * The altar uses a vanilla JSON cube model for its actual block faces; the
     * floating preview minion is drawn on top of that by
     * {@code AltarBlockEntityRenderer}.  Returning ENTITYBLOCK_ANIMATED here
     * would suppress the JSON model entirely and leave us with an invisible
     * block when the BER is missing.
     */
    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Override
    public VoxelShape getShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) {
        return SHAPE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityAltar(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> net.minecraft.world.level.block.entity.BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide
            ? createTickerHelper(type, NecromancyBlocks.ALTAR_BE.get(), BlockEntityAltar::clientTick)
            : null;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected net.minecraft.world.InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
            Player player, BlockHitResult hit) {
        if (level.isClientSide) return net.minecraft.world.InteractionResult.SUCCESS;
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof BlockEntityAltar altar)) return net.minecraft.world.InteractionResult.PASS;

        if (player.isShiftKeyDown()) {
            boolean ritualReady = altar.canSpawn() || player.getAbilities().instabuild;
            boolean partsOk = altar.partsConfiguredForSpawn();
            if (ritualReady && partsOk) {
                altar.spawn(player);
            } else if (!partsOk) {
                player.displayClientMessage(
                    net.minecraft.network.chat.Component.translatable(
                        "message.necromancy.altar.parts_unconfigured", altar.summarizeBlockedBodyParts()),
                    true);
            } else {
                player.displayClientMessage(
                    net.minecraft.network.chat.Component.translatable("message.necromancy.altar.missing_ingredients"),
                    true);
            }
            return net.minecraft.world.InteractionResult.CONSUME;
        }
        if (be instanceof MenuProvider mp) {
            player.openMenu(mp, buf -> buf.writeBlockPos(pos));
        }
        return net.minecraft.world.InteractionResult.CONSUME;
    }
}
