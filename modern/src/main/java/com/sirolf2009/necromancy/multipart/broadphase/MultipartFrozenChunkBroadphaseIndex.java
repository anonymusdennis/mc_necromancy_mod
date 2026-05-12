package com.sirolf2009.necromancy.multipart.broadphase;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Immutable chunk-column occupancy mirror for safe queries off the tick thread. */
final class MultipartFrozenChunkBroadphaseIndex {

    private final Map<Long, Set<Integer>> chunkToEntities;

    MultipartFrozenChunkBroadphaseIndex(Map<Long, Set<Integer>> chunkToEntities) {
        this.chunkToEntities = chunkToEntities;
    }

    static MultipartFrozenChunkBroadphaseIndex copyOf(ChunkEntityBroadphaseIndex src) {
        Map<Long, Set<Integer>> out = new HashMap<>(Math.max(16, src.chunkBucketCount()));
        src.copyChunkBucketsTo(out);
        return new MultipartFrozenChunkBroadphaseIndex(Map.copyOf(out));
    }

    Set<Integer> entityIdsOverlappingChunks(AABB box) {
        Set<Integer> out = new HashSet<>();
        int minChunkX = ChunkEntityBroadphaseIndex.blockToChunkCoord(box.minX);
        int maxChunkX = ChunkEntityBroadphaseIndex.blockToChunkCoord(box.maxX - 1e-7);
        int minChunkZ = ChunkEntityBroadphaseIndex.blockToChunkCoord(box.minZ);
        int maxChunkZ = ChunkEntityBroadphaseIndex.blockToChunkCoord(box.maxZ - 1e-7);
        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                long ck = ChunkPos.asLong(cx, cz);
                Set<Integer> ids = chunkToEntities.get(ck);
                if (ids != null) {
                    out.addAll(ids);
                }
            }
        }
        return out;
    }

    boolean isEmpty() {
        return chunkToEntities.isEmpty();
    }
}
