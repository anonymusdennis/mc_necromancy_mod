package com.sirolf2009.necromancy.multipart.network;

import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client-side mirror of {@linkplain com.sirolf2009.necromancy.multipart.editor.session.MultipartServerTopologyEditService server leases}
 * populated by {@link com.sirolf2009.necromancy.multipart.network.payload.MultipartEditLockNotifyPayload}.
 */
public final class MultipartClientEditLockCache {

    public record Entry(boolean locked, @Nullable UUID holderUuid, long sessionGeneration) {}

    private static final Map<Integer, Entry> BY_ENTITY = new ConcurrentHashMap<>();

    private MultipartClientEditLockCache() {}

    public static void apply(int entityId, boolean locked, @Nullable UUID holderUuid, long sessionGeneration) {
        if (!locked) {
            BY_ENTITY.remove(entityId);
        } else {
            BY_ENTITY.put(entityId, new Entry(true, holderUuid, sessionGeneration));
        }
    }

    public static @Nullable Entry probe(int entityId) {
        return BY_ENTITY.get(entityId);
    }

    public static void clear() {
        BY_ENTITY.clear();
    }
}
