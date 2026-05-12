package com.sirolf2009.necromancy.multipart.network.binary;

/**
 * Shared v1 blob prefix layout:
 *
 * <pre>
 * u8  schemaVersion
 * u16 flags LE ({@link MultipartPacketFlags})
 * u32 crc32 LE (present iff HAS_CRC32; computed over bytes [PAYLOAD_START .. end))
 * payload...
 * </pre>
 */
public final class MultipartReplicationHeader {

    public static final int SCHEMA_OFFSET = 0;
    public static final int FLAGS_OFFSET = 1;
    public static final int CRC_OFFSET = 3;
    /** First payload byte after optional CRC slot (CRC occupies bytes 3–6 inclusive when HAS_CRC32). */
    public static final int PAYLOAD_START_WITH_CRC = 7;
    public static final int PAYLOAD_START_NO_CRC = 3;

    private MultipartReplicationHeader() {}

    public static int payloadStart(int flags) {
        return (flags & MultipartPacketFlags.HAS_CRC32) != 0 ? PAYLOAD_START_WITH_CRC : PAYLOAD_START_NO_CRC;
    }
}
