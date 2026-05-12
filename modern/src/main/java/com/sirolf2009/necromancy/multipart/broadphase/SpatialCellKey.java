package com.sirolf2009.necromancy.multipart.broadphase;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

import java.util.function.LongConsumer;

/**
 * Uniform-grid cell keys for spatial hashing. Uses 21 bits per axis packed into a {@code long}
 * (adequate for Minecraft-scale worlds at typical cell sizes).
 */
public final class SpatialCellKey {

    private static final int AXIS_BITS = 21;
    private static final long AXIS_MASK = (1L << AXIS_BITS) - 1L;

    private SpatialCellKey() {
    }

    public static long pack(int cx, int cy, int cz) {
        long x = cx & AXIS_MASK;
        long y = cy & AXIS_MASK;
        long z = cz & AXIS_MASK;
        return x | (y << AXIS_BITS) | (z << (AXIS_BITS * 2));
    }

    /** Floored cell coordinate along one axis. */
    public static int cellCoord(double world, double invCellSize) {
        return Mth.floor(world * invCellSize);
    }

    /**
     * Invokes {@code consumer} with every cell key overlapping {@code box}.
     */
    public static void forEachCellOverlapping(AABB box, double cellSize, LongConsumer consumer) {
        if (cellSize <= 1e-9) {
            throw new IllegalArgumentException("cellSize must be positive");
        }
        double inv = 1.0 / cellSize;
        int minX = cellCoord(box.minX, inv);
        int minY = cellCoord(box.minY, inv);
        int minZ = cellCoord(box.minZ, inv);
        int maxX = cellCoord(box.maxX - 1e-7, inv);
        int maxY = cellCoord(box.maxY - 1e-7, inv);
        int maxZ = cellCoord(box.maxZ - 1e-7, inv);
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    consumer.accept(pack(x, y, z));
                }
            }
        }
    }

    /**
     * Number of uniform grid cells overlapping {@code box}. Matches {@link #forEachCellOverlapping} iteration count.
     * Returns a saturated value when the true count overflows practical thresholds.
     */
    public static int countCellsOverlapping(AABB box, double cellSize) {
        if (cellSize <= 1e-9) {
            throw new IllegalArgumentException("cellSize must be positive");
        }
        double inv = 1.0 / cellSize;
        int minX = cellCoord(box.minX, inv);
        int minY = cellCoord(box.minY, inv);
        int minZ = cellCoord(box.minZ, inv);
        int maxX = cellCoord(box.maxX - 1e-7, inv);
        int maxY = cellCoord(box.maxY - 1e-7, inv);
        int maxZ = cellCoord(box.maxZ - 1e-7, inv);
        long nx = (long) maxX - minX + 1;
        long ny = (long) maxY - minY + 1;
        long nz = (long) maxZ - minZ + 1;
        if (nx <= 0 || ny <= 0 || nz <= 0) {
            return 0;
        }
        long hi = nx * ny;
        if (hi > Integer.MAX_VALUE || nz > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        long total = hi * nz;
        if (total > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) total;
    }
}
