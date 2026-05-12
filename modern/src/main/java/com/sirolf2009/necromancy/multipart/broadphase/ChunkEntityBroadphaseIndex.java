package com.sirolf2009.necromancy.multipart.broadphase;

import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Coarse chunk-column index (16-block sections on X/Z). Useful for rejecting distant multipart roots before
 * consulting the spatial hash and for aligning work with vanilla chunk-loaded multiplayer locality.
 */
public final class ChunkEntityBroadphaseIndex {

    private final Map<Long, Set<Integer>> chunkToEntities = new HashMap<>();
    private final Map<Integer, Set<Long>> entityToChunks = new HashMap<>();

    public static int blockToChunkCoord(double blockCoord) {
        int block = Mth.floor(blockCoord);
        return block >> 4;
    }

    public void removeEntity(int entityId) {
        Set<Long> chunks = entityToChunks.remove(entityId);
        if (chunks == null) return;
        for (Long ck : chunks) {
            Set<Integer> set = chunkToEntities.get(ck);
            if (set != null) {
                set.remove(entityId);
                if (set.isEmpty()) chunkToEntities.remove(ck);
            }
        }
    }

    /**
     * Rebuild chunk occupancy from an axis-aligned union of all slots for one entity.
     */
    public void updateEntityChunks(int entityId, BroadphaseSlot unionLike) {
        removeEntity(entityId);
        var box = unionLike.bounds();
        int minChunkX = blockToChunkCoord(box.minX);
        int maxChunkX = blockToChunkCoord(box.maxX - 1e-7);
        int minChunkZ = blockToChunkCoord(box.minZ);
        int maxChunkZ = blockToChunkCoord(box.maxZ - 1e-7);

        Set<Long> occupied = new HashSet<>();
        entityToChunks.put(entityId, occupied);

        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                long ck = ChunkPos.asLong(cx, cz);
                occupied.add(ck);
                chunkToEntities.computeIfAbsent(ck, k -> new HashSet<>()).add(entityId);
            }
        }
    }

    /** Snapshot safe for iteration only; do not mutate the returned set when empty. */
    public Set<Integer> entitiesInChunk(long chunkPackedPos) {
        Set<Integer> s = chunkToEntities.get(chunkPackedPos);
        return s == null ? Collections.emptySet() : s;
    }

    public Set<Long> chunksForEntity(int entityId) {
        Set<Long> s = entityToChunks.get(entityId);
        return s == null ? Collections.emptySet() : s;
    }

    public void clear() {
        chunkToEntities.clear();
        entityToChunks.clear();
    }

    /** Package-private: shallow bucket count hint for snapshot sizing. */
    int chunkBucketCount() {
        return chunkToEntities.size();
    }

    /**
     * Package-private: fills {@code out} with immutable {@link Set#copyOf} values keyed by packed chunk position.
     */
    void copyChunkBucketsTo(Map<Long, Set<Integer>> out) {
        for (var e : chunkToEntities.entrySet()) {
            out.put(e.getKey(), Set.copyOf(e.getValue()));
        }
    }
}
