package com.sirolf2009.necromancy.multipart.sync;

/**
 * Documents sync strategy for multipart mobs (ECS-friendly "dirty" boundary).
 *
 * <p><strong>Server authoritative:</strong> topology ({@link com.sirolf2009.necromancy.multipart.TransformHierarchy#topologyRevision()}),
 * assembly epochs ({@link com.sirolf2009.necromancy.multipart.TransformHierarchy#hierarchyCommittedRevision()}),
 * detach flags, and part definitions / items equipped on sockets.
 *
 * <p><strong>Minimal wire:</strong> bump a monotonic revision when assembly changes; clients rebuild local graph from the
 * same revision payload (or rebuild from existing synced inventory slots). Avoid syncing full transforms—recompute from root.
 *
 * <p><strong>Packets:</strong>
 * {@link com.sirolf2009.necromancy.multipart.network.payload.MultipartTopologyNotifyPayload},
 * {@link com.sirolf2009.necromancy.multipart.network.payload.MultipartTransformDeltaPayload}, and
 * {@link com.sirolf2009.necromancy.multipart.network.payload.MultipartDamageSyncPayload} carry sparse deltas — decode
 * inside {@link com.sirolf2009.necromancy.multipart.RootMobEntity} hooks.
 */
public final class MultipartAssemblyRevision {

    private MultipartAssemblyRevision() {}

    /** Convention: pack topologyRevision into synched entity data or companion packet when implementing RootMobEntity. */
    public static int encodeTopologyFlag(int topologyRevision, boolean dirtyLocalOffsets) {
        return (topologyRevision & 0x7FFF_FFFF) | (dirtyLocalOffsets ? 0x8000_0000 : 0);
    }

    public static int topologyPart(int encoded) {
        return encoded & 0x7FFF_FFFF;
    }

    public static boolean offsetsDirty(int encoded) {
        return (encoded & 0x8000_0000) != 0;
    }
}
