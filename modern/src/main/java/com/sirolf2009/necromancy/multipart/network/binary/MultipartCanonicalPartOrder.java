package com.sirolf2009.necromancy.multipart.network.binary;

import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;

/**
 * Canonical multipart part ordering for compact sorted indices on the wire. Encoder and decoder must share this sort key.
 */
public final class MultipartCanonicalPartOrder {

    private static final Comparator<ResourceLocation> BY_STRING = Comparator.comparing(ResourceLocation::toString);

    private MultipartCanonicalPartOrder() {}

    public static ResourceLocation[] sortedIds(Collection<ResourceLocation> ids) {
        ArrayList<ResourceLocation> list = new ArrayList<>(ids);
        list.sort(BY_STRING);
        return list.toArray(ResourceLocation[]::new);
    }

    /** @return sorted index or negative if absent */
    public static int sortedIndexOf(ResourceLocation[] sortedAsc, ResourceLocation id) {
        return Arrays.binarySearch(sortedAsc, id, BY_STRING);
    }
}
