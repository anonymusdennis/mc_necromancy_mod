package com.sirolf2009.necromancy.multipart.broadphase;

import net.minecraft.world.entity.EntityType;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/** Pluggable per-{@link EntityType} subtree accelerators for grouped ray queries ({@link MultipartBroadphaseQueryStrategies#subtreeGrouped}). */
public final class MultipartBroadphaseEntityAcceleratorRegistry {

    private static final ConcurrentHashMap<EntityType<?>, MultipartBroadphaseEntityAccelerator> MAP = new ConcurrentHashMap<>();

    private MultipartBroadphaseEntityAcceleratorRegistry() {
    }

    public static void register(EntityType<?> type, MultipartBroadphaseEntityAccelerator accelerator) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(accelerator, "accelerator");
        MAP.put(type, accelerator);
    }

    public static MultipartBroadphaseEntityAccelerator get(EntityType<?> type) {
        if (type == null) {
            return MultipartBroadphaseEntityAccelerator.PASSTHROUGH;
        }
        return MAP.getOrDefault(type, MultipartBroadphaseEntityAccelerator.PASSTHROUGH);
    }
}
