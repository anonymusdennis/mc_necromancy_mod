package com.sirolf2009.necromancy.multipart.network.binary;

/** Wire blob schema ids embedded as first byte of each {@code packed*} byte array. Evolution policy: never reuse ids. */
public final class MultipartSchemaVersions {

    /** Transform delta blob (simulation locals + sparse attachment bits). */
    public static final byte TRANSFORM_DELTA_V1 = 1;

    /** Damage / sever / functional flags snapshot. */
    public static final byte DAMAGE_SYNC_V1 = 1;

    /** Surgical topology / attachment graph edits (ResourceLocation-addressed). */
    public static final byte TOPOLOGY_DELTA_V1 = 1;

    private MultipartSchemaVersions() {}
}
