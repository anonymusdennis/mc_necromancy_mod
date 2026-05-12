package com.sirolf2009.necromancy.api.feature;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Static registry of {@link PartFeature} singletons keyed by their {@link PartFeature#id()}.
 *
 * <p>Registry use is optional: adapters may also return ad-hoc feature
 * instances directly.  The registry exists so unrelated subsystems (UI,
 * commands, JEI integration, save data round-trips, ...) can reference a
 * feature by id without holding a hard dependency on the implementation
 * class.
 */
public final class FeatureRegistry {

    private FeatureRegistry() {}

    private static final Map<String, PartFeature> BY_ID = new LinkedHashMap<>();

    /**
     * Register a feature singleton.  If the same id is registered twice the
     * second call is silently ignored (so adapter classes can re-register
     * defensively without needing to know about load order).
     *
     * @return the canonical singleton stored under this id
     */
    public static synchronized <T extends PartFeature> T register(T feature) {
        BY_ID.putIfAbsent(feature.id(), feature);
        @SuppressWarnings("unchecked")
        T canonical = (T) BY_ID.get(feature.id());
        return canonical;
    }

    /** Look up a feature by id, or {@code null} if not registered. */
    public static synchronized PartFeature get(String id) { return BY_ID.get(id); }

    /** Snapshot of every registered feature, in insertion order. */
    public static synchronized Collection<PartFeature> all() {
        return Collections.unmodifiableCollection(BY_ID.values());
    }
}
