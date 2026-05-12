package com.sirolf2009.necromancy.multipart.broadphase;

import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Optional narrow-phase accelerator for all broad-phase slots belonging to one multipart root ({@link BroadphaseSlot#entityId()}).
 * Registered per {@link net.minecraft.world.entity.EntityType} via {@link MultipartBroadphaseEntityAcceleratorRegistry}.
 */
@FunctionalInterface
public interface MultipartBroadphaseEntityAccelerator {

    MultipartBroadphaseEntityAccelerator PASSTHROUGH = (origin, dirN, maxDist, entitySlots, fallback) ->
        fallback.filterAlongRay(origin, dirN, maxDist, entitySlots);

    List<BroadphaseSlot> refineAlongRay(Vec3 origin, Vec3 dirN, double maxDist, List<BroadphaseSlot> entitySlots,
                                        MultipartInternalBroadphase fallback);
}
