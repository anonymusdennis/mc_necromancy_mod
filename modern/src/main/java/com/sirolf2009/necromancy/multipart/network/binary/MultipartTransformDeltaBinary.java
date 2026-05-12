package com.sirolf2009.necromancy.multipart.network.binary;

import com.sirolf2009.necromancy.multipart.math.PartTransform;
import net.minecraft.network.FriendlyByteBuf;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Compact multipart simulation-local deltas keyed by canonical sorted part index.
 *
 * <p>Interpolation contract: apply poses before the next {@code multipartTick()} so {@link com.sirolf2009.necromancy.multipart.RootMobEntity#multipartPoseInterpolationCapture()}
 * observes stable prev/current pairs {@linkplain com.sirolf2009.necromancy.multipart.network.MultipartReplicationBridge documented there}.
 */
public final class MultipartTransformDeltaBinary {

    public static final byte OP_SIM_LOCAL_FULL = 1;
    public static final byte OP_ATTACHED = 2;

    public record SimLocalOp(int sortedPartIndex, PartTransform localPose) {}

    public record AttachedOp(int sortedPartIndex, boolean attachedToParent) {}

    /** Consume decoded ops (reuse {@link MultipartReplicationScratch#transformScratch()} across callbacks — overwrite between calls). */
    public interface Sink {
        void onSimLocal(int sortedPartIndex, PartTransform reusablePoseReadOnlyViaScratch);

        void onAttached(int sortedPartIndex, boolean attachedToParent);
    }

    private MultipartTransformDeltaBinary() {}

    /**
     * @param topologyRevisionGuard replicated topology revision clients must match
     */
    public static byte[] encodeV1(int topologyRevisionGuard, int flags, List<SimLocalOp> simLocals,
                                  List<AttachedOp> attached, MultipartReplicationScratch scratch) {
        FriendlyByteBuf b = scratch.buf();
        scratch.resetBuf();
        boolean crc = (flags & MultipartPacketFlags.HAS_CRC32) != 0;
        boolean half = (flags & MultipartPacketFlags.HALF_PRECISION_TRS) != 0;
        if ((flags & MultipartPacketFlags.SMALLEST_THREE_ROTATION) != 0) {
            throw new IllegalArgumentException("SMALLEST_THREE_ROTATION not implemented yet");
        }

        b.writeByte(MultipartSchemaVersions.TRANSFORM_DELTA_V1);
        b.writeShortLE(flags & 0xFFFF);
        int crcSlot = -1;
        if (crc) {
            crcSlot = b.writerIndex();
            b.writeIntLE(0);
        }

        b.writeVarInt(topologyRevisionGuard);
        int totalOps = simLocals.size() + attached.size();
        b.writeVarInt(totalOps);

        Quaternionf q = scratch.quatScratch();
        Vector3f v = scratch.vecScratch();

        for (SimLocalOp op : simLocals) {
            b.writeByte(OP_SIM_LOCAL_FULL);
            b.writeVarInt(op.sortedPartIndex());
            MultipartPackedTrsCodec.writeSimLocal(b, op.localPose(), half, q, v);
        }
        for (AttachedOp op : attached) {
            b.writeByte(OP_ATTACHED);
            b.writeVarInt(op.sortedPartIndex());
            b.writeBoolean(op.attachedToParent());
        }

        if (crc) {
            byte[] blob = scratch.copyBufToArray();
            int crcVal = MultipartReplicationCrc32.compute(blob, MultipartReplicationHeader.PAYLOAD_START_WITH_CRC, blob.length);
            blob[crcSlot] = (byte) (crcVal & 0xFF);
            blob[crcSlot + 1] = (byte) ((crcVal >>> 8) & 0xFF);
            blob[crcSlot + 2] = (byte) ((crcVal >>> 16) & 0xFF);
            blob[crcSlot + 3] = (byte) ((crcVal >>> 24) & 0xFF);
            return blob;
        }
        return scratch.copyBufToArray();
    }

    public static MultipartDecodeStatus decodeV1(byte[] blob, int clientTopologyRevision, Sink sink, MultipartReplicationScratch scratch) {
        if (blob == null || blob.length < MultipartReplicationHeader.PAYLOAD_START_NO_CRC) {
            return MultipartDecodeStatus.TRUNCATED;
        }
        scratch.wrapRead(blob);
        FriendlyByteBuf b = scratch.buf();
        byte schema = b.readByte();
        if (schema != MultipartSchemaVersions.TRANSFORM_DELTA_V1) {
            return MultipartDecodeStatus.UNKNOWN_SCHEMA;
        }
        int flags = b.readUnsignedShortLE();
        if ((flags & MultipartPacketFlags.SMALLEST_THREE_ROTATION) != 0) {
            return MultipartDecodeStatus.UNSUPPORTED_FLAGS;
        }
        int payloadStart = MultipartReplicationHeader.payloadStart(flags);
        if (blob.length < payloadStart) {
            return MultipartDecodeStatus.TRUNCATED;
        }
        if ((flags & MultipartPacketFlags.HAS_CRC32) != 0) {
            int wireCrc = b.readIntLE();
            int computed = MultipartReplicationCrc32.compute(blob, MultipartReplicationHeader.PAYLOAD_START_WITH_CRC, blob.length);
            if (wireCrc != computed) {
                return MultipartDecodeStatus.BAD_CRC;
            }
        }
        scratch.readerAtPayloadStart(payloadStart);

        int topoGuard = readVarIntChecked(b);
        if (topoGuard != clientTopologyRevision) {
            return MultipartDecodeStatus.TOPOLOGY_MISMATCH;
        }
        int opCount = readVarIntChecked(b);
        if (opCount < 0) {
            return MultipartDecodeStatus.TRUNCATED;
        }

        boolean half = (flags & MultipartPacketFlags.HALF_PRECISION_TRS) != 0;
        Quaternionf q = scratch.quatScratch();
        Vector3f v = scratch.vecScratch();
        PartTransform pose = scratch.transformScratch();

        for (int i = 0; i < opCount; i++) {
            if (!b.isReadable()) {
                return MultipartDecodeStatus.TRUNCATED;
            }
            byte op = b.readByte();
            switch (op) {
                case OP_SIM_LOCAL_FULL -> {
                    int idx = readVarIntChecked(b);
                    MultipartPackedTrsCodec.readSimLocal(b, pose, half, q, v);
                    sink.onSimLocal(idx, pose);
                }
                case OP_ATTACHED -> {
                    int idx = readVarIntChecked(b);
                    sink.onAttached(idx, b.readBoolean());
                }
                default -> {
                    return MultipartDecodeStatus.ILLEGAL_OPCODE;
                }
            }
        }
        // Ignore trailing bytes — forward-compatible wire evolution may pad or append ignored extensions.
        return MultipartDecodeStatus.OK;
    }

    private static int readVarIntChecked(FriendlyByteBuf b) {
        if (!b.isReadable()) {
            return -1;
        }
        return b.readVarInt();
    }

    /** Debug helper producing immutable batches without touching replication hooks. */
    public static List<String> describe(byte[] blob) {
        ArrayList<String> lines = new ArrayList<>();
        if (blob == null || blob.length == 0) {
            lines.add("empty blob");
            return lines;
        }
        lines.add("schema=%02x length=%d".formatted(blob[0] & 0xFF, blob.length));
        return lines;
    }
}
