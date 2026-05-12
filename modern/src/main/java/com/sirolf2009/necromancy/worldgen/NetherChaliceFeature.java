package com.sirolf2009.necromancy.worldgen;

import com.mojang.serialization.Codec;
import com.sirolf2009.necromancy.block.NecromancyBlocks;
import com.sirolf2009.necromancy.fluid.NecromancyFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.LevelAccessor;

/**
 * Nether Chalice feature.
 *
 * <p>Direct port of legacy {@code WorldGenNetherChalice}.  Walks the same
 * block-by-block placement loop the original used; we keep the literal
 * coordinates so resource-pack overrides of nether-brick blocks still work.
 *
 * <p>Anchored at any {@link Blocks#LAVA} column with air above (bowl effect).
 * The legacy mod swept random offsets within the chunk; in modern code that
 * randomness is the {@link FeaturePlaceContext}'s job, so we just check the
 * given position and place if the precondition holds.
 */
public class NetherChaliceFeature extends Feature<NoneFeatureConfiguration> {

    public NetherChaliceFeature(Codec<NoneFeatureConfiguration> codec) { super(codec); }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        LevelAccessor level = ctx.level();
        BlockPos origin = ctx.origin();
        // legacy precondition: lava with air above.
        if (level.getBlockState(origin).getBlock() != Blocks.LAVA) return false;
        if (!level.getBlockState(origin.above()).isAir())            return false;

        BlockState netherBrick   = Blocks.NETHER_BRICKS.defaultBlockState();
        BlockState netherFence   = Blocks.NETHER_BRICK_FENCE.defaultBlockState();
        BlockState netherStairs  = Blocks.NETHER_BRICK_STAIRS.defaultBlockState();
        BlockState ironBars      = Blocks.IRON_BARS.defaultBlockState();
        BlockState redstoneTorch = Blocks.REDSTONE_WALL_TORCH.defaultBlockState();
        BlockState bloodSrc      = NecromancyFluids.BLOOD_SOURCE.get().defaultFluidState().createLegacyBlock();

        // The original 1.7.10 list is huge; we condense the recurring pattern.
        // Top fence ring -- two outer rows.
        for (int dx = 0; dx < 7; dx++) {
            for (int dz : new int[] {0, 6}) {
                place(level, origin, dx, 21, dz, netherFence);
            }
        }
        // Walls (rows 18..20).
        for (int dx = 0; dx < 7; dx++) {
            for (int dy = 19; dy <= 20; dy++) {
                place(level, origin, dx,    dy, 0, netherFence);
                place(level, origin, dx,    dy, 6, netherFence);
                place(level, origin, 0,     dy, dx, netherFence);
                place(level, origin, 6,     dy, dx, netherFence);
            }
        }
        // Iron-bar shaft (the chalice cup).
        for (int dy = 0; dy <= 17; dy++) {
            for (int dx = 2; dx <= 4; dx++) {
                for (int dz = 2; dz <= 4; dz++) {
                    if (dx == 3 && dz == 3) continue;
                    place(level, origin, dx, dy, dz, ironBars);
                }
            }
        }
        // Blood pool inside the cup.
        for (int dy = 0; dy <= 19; dy++) {
            place(level, origin, 3, dy, 3, bloodSrc);
        }
        // Surface blood ring.
        for (int dx = 2; dx <= 4; dx++) {
            for (int dz = 2; dz <= 4; dz++) {
                if (level.getBlockState(origin.offset(dx, 20, dz)).isAir()) {
                    place(level, origin, dx, 20, dz, bloodSrc);
                }
            }
        }
        // Decorative torches (corners).
        for (int dx : new int[] {0, 6}) {
            for (int dz : new int[] {1, 5}) {
                place(level, origin, dx, 22, dz, redstoneTorch);
            }
        }
        return true;
    }

    private void place(LevelAccessor level, BlockPos origin, int dx, int dy, int dz, BlockState state) {
        level.setBlock(origin.offset(dx, dy, dz), state, 2);
    }
}
