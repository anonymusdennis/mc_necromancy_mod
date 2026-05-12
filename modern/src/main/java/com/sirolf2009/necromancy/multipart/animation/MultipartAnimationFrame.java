package com.sirolf2009.necromancy.multipart.animation;

import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Per-tick scratch container: animation behaviors push {@link WeightedPartTransform} entries per bodypart.
 * Cleared/rebuilt each simulation tick before {@link com.sirolf2009.necromancy.multipart.TransformHierarchy#tick}.
 */
public final class MultipartAnimationFrame {

    private final Map<ResourceLocation, ArrayList<WeightedPartTransform>> layersByPart = new HashMap<>();

    public void clear() {
        layersByPart.clear();
    }

    public void addLayer(ResourceLocation partId, WeightedPartTransform layer) {
        layersByPart.computeIfAbsent(partId, k -> new ArrayList<>(4)).add(layer);
    }

    /** Mutable backing for accumulation — do not retain references across ticks. */
    public List<WeightedPartTransform> layersFor(ResourceLocation partId) {
        List<WeightedPartTransform> list = layersByPart.get(partId);
        return list != null ? list : List.of();
    }

    /**
     * Sorted snapshot safe for evaluation without reordering stored lists.
     */
    public List<WeightedPartTransform> sortedLayersFor(ResourceLocation partId) {
        List<WeightedPartTransform> raw = layersFor(partId);
        if (raw.isEmpty()) {
            return List.of();
        }
        ArrayList<WeightedPartTransform> sorted = new ArrayList<>(raw);
        sorted.sort(Comparator.comparingInt((WeightedPartTransform w) -> w.phase().ordinal())
            .thenComparingInt(WeightedPartTransform::priority));
        return sorted;
    }
}
