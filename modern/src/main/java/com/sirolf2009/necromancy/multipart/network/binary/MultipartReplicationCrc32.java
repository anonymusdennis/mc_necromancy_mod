package com.sirolf2009.necromancy.multipart.network.binary;

import java.util.zip.CRC32;

/** Allocation-free CRC over payload slices (Java {@link CRC32}, IEEE polynomial). */
public final class MultipartReplicationCrc32 {

    private MultipartReplicationCrc32() {}

    public static int compute(byte[] blob, int fromInclusive, int toExclusive) {
        CRC32 crc = new CRC32();
        crc.update(blob, fromInclusive, toExclusive - fromInclusive);
        return (int) crc.getValue();
    }
}
