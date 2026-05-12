package com.sirolf2009.necromancy.multipart.broadphase;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

/**
 * One broad-phase entry: an axis-aligned bound in world space with stable identity for deduplication.
 *
 * @param aggregate When {@code true}, {@code partId} is unused (single proxy bound for dormant / merged indexing).
 */
public record BroadphaseSlot(
    int entityId,
    @Nullable ResourceLocation partId,
    AABB bounds,
    long propagationSerial,
    boolean aggregate
) {
    public static BroadphaseSlot part(int entityId, ResourceLocation partId, AABB bounds, long propagationSerial) {
        return new BroadphaseSlot(entityId, partId, bounds, propagationSerial, false);
    }

    public static BroadphaseSlot aggregate(int entityId, AABB bounds, long propagationSerial) {
        return new BroadphaseSlot(entityId, null, bounds, propagationSerial, true);
    }
}
