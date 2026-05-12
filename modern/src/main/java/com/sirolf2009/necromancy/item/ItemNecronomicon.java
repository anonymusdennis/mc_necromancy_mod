package com.sirolf2009.necromancy.item;

import com.sirolf2009.necromancy.block.NecromancyBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.Map;
import java.util.Map.Entry;

/**
 * The Necronomicon -- summons the altar from a "planks + 2 cobble" pattern in
 * any of the four cardinal directions, and acts as a remote control for your
 * minions when right-clicked in the air.
 *
 * <p>Direct port of {@code ItemNecronomicon}.  The pattern recognition matches
 * the legacy mod 1:1 (plank as anchor, two cobblestones in line).  The 1.7.10
 * mod awarded an achievement here; we keep the achievement criteria via the
 * vanilla advancement system in {@code data/necromancy/advancement/}.
 */
public class ItemNecronomicon extends Item {

    /** Pattern matrix: which direction needs cobble adjacent for each rotation. */
    private static final Map<Direction, Integer> ROTATIONS = Map.of(
        Direction.EAST,  3,
        Direction.WEST,  1,
        Direction.SOUTH, 0,
        Direction.NORTH, 2
    );

    public ItemNecronomicon() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level   = ctx.getLevel();
        BlockPos pos  = ctx.getClickedPos();
        Player player = ctx.getPlayer();
        if (player == null) return InteractionResult.PASS;

        BlockState clicked = level.getBlockState(pos);
        if (!clicked.is(Blocks.OAK_PLANKS) && !clicked.is(Blocks.SPRUCE_PLANKS)
            && !clicked.is(Blocks.BIRCH_PLANKS) && !clicked.is(Blocks.JUNGLE_PLANKS)
            && !clicked.is(Blocks.ACACIA_PLANKS) && !clicked.is(Blocks.DARK_OAK_PLANKS)
            && !clicked.is(Blocks.MANGROVE_PLANKS) && !clicked.is(Blocks.CHERRY_PLANKS)
            && !clicked.is(Blocks.CRIMSON_PLANKS) && !clicked.is(Blocks.WARPED_PLANKS)
            && !clicked.is(Blocks.BAMBOO_PLANKS)) {
            return InteractionResult.PASS;
        }

        // Probe each cardinal direction for the "2 cobblestones in a row" pattern.
        for (Entry<Direction, Integer> e : ROTATIONS.entrySet()) {
            Direction dir = e.getKey();
            BlockPos a = pos.relative(dir);
            BlockPos b = pos.relative(dir, 2);
            if (level.getBlockState(a).is(Blocks.COBBLESTONE)
                && level.getBlockState(b).is(Blocks.COBBLESTONE)) {

                // Build the altar.  Facing is encoded in the legacy direction
                // index via BlockAltar's HORIZONTAL_FACING property.
                Direction face = dir;  // Altar faces opposite of the cobbles
                BlockState altar = NecromancyBlocks.ALTAR.get().defaultBlockState()
                    .setValue(BlockStateProperties.HORIZONTAL_FACING, face);
                BlockState altarBlock = NecromancyBlocks.ALTAR_BLOCK.get().defaultBlockState();

                level.setBlock(pos, altar, 3);
                level.setBlock(a, altarBlock, 3);
                level.setBlock(b, altarBlock, 3);

                if (!player.getAbilities().instabuild) {
                    ctx.getItemInHand().shrink(1);
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            // Client-side open the minion management screen.
            com.sirolf2009.necromancy.client.NecromancyClient.openNecronomiconScreen();
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
