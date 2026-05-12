package com.sirolf2009.necromancy.multipart.network.binary;

/** Bitmask stored LE u16 immediately after schema byte in v1 blobs. */
public final class MultipartPacketFlags {

    /** CRC-32 (IEEE) over payload bytes starting at {@link MultipartReplicationHeader#PAYLOAD_START}. */
    public static final int HAS_CRC32 = 1;

    /** Quaternion + translation + scale written as float16 components instead of float32. */
    public static final int HALF_PRECISION_TRS = 2;

    /** Future: smallest-three quaternion packing without FP16 requirement. */
    public static final int SMALLEST_THREE_ROTATION = 4;

    private MultipartPacketFlags() {}
}
