package com.sirolf2009.necromancy.multipart.broadphase;

import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Narrow-phase hook applied after global spatial-hash / chunk culling ({@link MultipartBroadphaseQueryStrategy} chooses how
 * candidates are gathered and ordered before this seam runs — linear scan, Morton ordering, subtree grouping, future BVH-backed streams).
 */
public interface MultipartInternalBroadphase {

    List<BroadphaseSlot> filterAlongRay(Vec3 origin, Vec3 direction, double maxDistance, List<BroadphaseSlot> parts);

    /** Simple linear implementation — swap for a BVH when profiling shows hotspots on huge bosses. */
    final class LinearScan implements MultipartInternalBroadphase {
        @Override
        public List<BroadphaseSlot> filterAlongRay(Vec3 origin, Vec3 direction, double maxDistance,
                                                     List<BroadphaseSlot> parts) {
            List<BroadphaseSlot> hit = new ArrayList<>();
            for (BroadphaseSlot s : new ArrayList<>(parts)) {
                if (BroadphaseQueries.rayIntersectsAabb(origin, direction, maxDistance, s.bounds())) {
                    hit.add(s);
                }
            }
            return hit;
        }
    }

    /** Stable ordering by Morton code of slot center (cache-friendly traversal for wide bosses). */
    final class MortonOrderedLinearScan implements MultipartInternalBroadphase {
        private static final Comparator<BroadphaseSlot> BY_MORTON =
            Comparator.comparingLong(MortonOrderedLinearScan::morton3);

        @Override
        public List<BroadphaseSlot> filterAlongRay(Vec3 origin, Vec3 direction, double maxDistance,
                                                   List<BroadphaseSlot> parts) {
            List<BroadphaseSlot> sorted = new ArrayList<>(parts);
            sorted.sort(BY_MORTON);
            List<BroadphaseSlot> hit = new ArrayList<>();
            for (BroadphaseSlot s : sorted) {
                if (BroadphaseQueries.rayIntersectsAabb(origin, direction, maxDistance, s.bounds())) {
                    hit.add(s);
                }
            }
            return hit;
        }

        private static long morton3(BroadphaseSlot s) {
            var c = s.bounds().getCenter();
            int x = quant(c.x);
            int y = quant(c.y);
            int z = quant(c.z);
            return part1By2(x & 0x1FF) | (part1By2(y & 0x1FF) << 1) | (part1By2(z & 0x1FF) << 2);
        }

        private static int quant(double v) {
            return (int) Math.floor(v * 8.0);
        }

        private static long part1By2(int n) {
            long x = n & 0x1FFL;
            x = (x | (x << 16)) & 0x000000000000FFFFL;
            x = (x | (x << 8)) & 0x0000000000FF00FFL;
            x = (x | (x << 4)) & 0x000000000F0F0F0FL;
            x = (x | (x << 2)) & 0x0000000033333333L;
            x = (x | (x << 1)) & 0x0000000055555555L;
            return x;
        }
    }
}
