package com.sirolf2009.necromancy.multipart.network.binary;

import com.sirolf2009.necromancy.multipart.math.PartTransform;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/** Dry-run preflight + debug helpers for replication blobs. */
public final class MultipartReplicationValidate {

    private static final MultipartTransformDeltaBinary.Sink NOOP_TRANSFORM = new MultipartTransformDeltaBinary.Sink() {
        @Override
        public void onSimLocal(int sortedPartIndex, PartTransform reusablePoseReadOnlyViaScratch) {
        }

        @Override
        public void onAttached(int sortedPartIndex, boolean attachedToParent) {
        }
    };

    private static final MultipartDamageDeltaBinary.Sink NOOP_DAMAGE =
        (sortedPartIndex, curHp, maxHp, functionalFlagBits, severed, destroyed, mask) -> {
        };

    private static final MultipartTopologyDeltaBinary.Sink NOOP_TOPO =
        (parentPart, socketId, childPartOrNull) -> {
        };

    private MultipartReplicationValidate() {}

    public static MultipartDecodeStatus preflightTransformV1(byte[] blob, int topologyRevision) {
        MultipartReplicationScratch s = new MultipartReplicationScratch();
        return MultipartTransformDeltaBinary.decodeV1(blob, topologyRevision, NOOP_TRANSFORM, s);
    }

    public static MultipartDecodeStatus preflightDamageV1(byte[] blob, int topologyRevision) {
        MultipartReplicationScratch s = new MultipartReplicationScratch();
        return MultipartDamageDeltaBinary.decodeV1(blob, topologyRevision, NOOP_DAMAGE, s);
    }

    public static MultipartDecodeStatus preflightTopologyV1(byte[] blob, int topologyRevision) {
        MultipartReplicationScratch s = new MultipartReplicationScratch();
        return MultipartTopologyDeltaBinary.decodeV1(blob, topologyRevision, NOOP_TOPO, s);
    }

    public static String summarize(byte[] blob) {
        if (blob == null || blob.length == 0) {
            return "empty";
        }
        int schema = blob[0] & 0xFF;
        if (blob.length < MultipartReplicationHeader.PAYLOAD_START_NO_CRC) {
            return "schema=0x%02x bytes=%d truncated_header".formatted(schema, blob.length);
        }
        int flags = ByteBuffer.wrap(blob).order(ByteOrder.LITTLE_ENDIAN).getShort(MultipartReplicationHeader.FLAGS_OFFSET) & 0xFFFF;
        int payloadStart = MultipartReplicationHeader.payloadStart(flags);
        String crcInfo = "absent";
        if ((flags & MultipartPacketFlags.HAS_CRC32) != 0) {
            if (blob.length >= MultipartReplicationHeader.PAYLOAD_START_WITH_CRC) {
                int wire = ByteBuffer.wrap(blob).order(ByteOrder.LITTLE_ENDIAN).getInt(MultipartReplicationHeader.CRC_OFFSET);
                crcInfo = "0x%08x".formatted(wire);
            } else {
                crcInfo = "truncated";
            }
        }
        return ("schema=0x%02x bytes=%d flags=0x%04x half_trs=%s crc=%s payload_offset=%d")
            .formatted(schema, blob.length, flags,
                ((flags & MultipartPacketFlags.HALF_PRECISION_TRS) != 0),
                crcInfo, payloadStart);
    }
}
