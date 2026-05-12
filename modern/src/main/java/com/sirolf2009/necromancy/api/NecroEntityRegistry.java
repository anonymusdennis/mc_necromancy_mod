package com.sirolf2009.necromancy.api;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Static registry that adapter classes call into during common-setup.
 *
 * <p>Direct port of {@code com.sirolf2009.necroapi.NecroEntityRegistry}.
 * We use {@link LinkedHashMap} so iteration order is deterministic and matches
 * adapter registration order (important for recipe ordering and creative-tab
 * layout).
 */
public final class NecroEntityRegistry {

    /** Map of mob name -> adapter, keyed exactly like the legacy registry. */
    public static final Map<String, NecroEntityBase> registeredEntities = new LinkedHashMap<>();

    /** Subset of {@link #registeredEntities} that also implement {@link ISkull}. */
    public static final Map<String, ISkull> registeredSkullEntities = new LinkedHashMap<>();

    private NecroEntityRegistry() {}

    public static void registerEntity(NecroEntityBase data) {
        if (data.isNecromancyInstalled && !registeredEntities.containsKey(data.mobName)) {
            registeredEntities.put(data.mobName, data);
            if (data instanceof ISkull s) {
                registeredSkullEntities.put(data.mobName, s);
            }
        }
    }

    public static NecroEntityBase get(String mobName) {
        return registeredEntities.get(mobName);
    }
}
