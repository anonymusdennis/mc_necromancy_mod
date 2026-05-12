package com.sirolf2009.necromancy.multipart.network.binary;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * ResourceLocation-addressed surgical topology edits (no full graph snapshot).
 */
public final class MultipartTopologyDeltaBinary {

    public static final byte OP_SOCKET_ASSIGN_V1 = 1;

    public record SocketAssignOp(ResourceLocation parentPart, ResourceLocation socketId, ResourceLocation childPartOrNull) {}

    public interface Sink {
        void onSocketAssign(ResourceLocation parentPart, ResourceLocation socketId, ResourceLocation childPartOrNull);
    }

    private MultipartTopologyDeltaBinary() {}

    public static byte[] encodeV1(int topologyRevisionGuard, int flags, List<SocketAssignOp> ops,
                                  MultipartReplicationScratch scratch) {
        FriendlyByteBuf b = scratch.buf();
        scratch.resetBuf();
        boolean crc = (flags & MultipartPacketFlags.HAS_CRC32) != 0;

        b.writeByte(MultipartSchemaVersions.TOPOLOGY_DELTA_V1);
        b.writeShortLE(flags & 0xFFFF);
        int crcSlot = -1;
        if (crc) {
            crcSlot = b.writerIndex();
            b.writeIntLE(0);
        }

        b.writeVarInt(topologyRevisionGuard);
        b.writeVarInt(ops.size());

        for (SocketAssignOp op : ops) {
            b.writeByte(OP_SOCKET_ASSIGN_V1);
            MultipartNetRlCodec.writeRl(b, op.parentPart());
            MultipartNetRlCodec.writeRl(b, op.socketId());
            boolean hasChild = op.childPartOrNull() != null;
            b.writeBoolean(hasChild);
            if (hasChild) {
                MultipartNetRlCodec.writeRl(b, op.childPartOrNull());
            }
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

    public static MultipartDecodeStatus decodeV1(byte[] blob, int clientTopologyRevision,
                                                 Sink sink, MultipartReplicationScratch scratch) {
        if (blob == null || blob.length < MultipartReplicationHeader.PAYLOAD_START_NO_CRC) {
            return MultipartDecodeStatus.TRUNCATED;
        }
        scratch.wrapRead(blob);
        FriendlyByteBuf b = scratch.buf();
        if (b.readByte() != MultipartSchemaVersions.TOPOLOGY_DELTA_V1) {
            return MultipartDecodeStatus.UNKNOWN_SCHEMA;
        }
        int flags = b.readUnsignedShortLE();
        int payloadStart = MultipartReplicationHeader.payloadStart(flags);
        if (blob.length < payloadStart) {
            return MultipartDecodeStatus.TRUNCATED;
        }
        if ((flags & MultipartPacketFlags.HAS_CRC32) != 0) {
            int wire = b.readIntLE();
            int computed = MultipartReplicationCrc32.compute(blob, MultipartReplicationHeader.PAYLOAD_START_WITH_CRC, blob.length);
            if (wire != computed) {
                return MultipartDecodeStatus.BAD_CRC;
            }
        }
        scratch.readerAtPayloadStart(payloadStart);

        int topo = b.readVarInt();
        if (topo != clientTopologyRevision) {
            return MultipartDecodeStatus.TOPOLOGY_MISMATCH;
        }
        int count = b.readVarInt();
        for (int i = 0; i < count; i++) {
            if (!b.isReadable()) {
                return MultipartDecodeStatus.TRUNCATED;
            }
            byte op = b.readByte();
            if (op != OP_SOCKET_ASSIGN_V1) {
                return MultipartDecodeStatus.ILLEGAL_OPCODE;
            }
            ResourceLocation parent = MultipartNetRlCodec.readRl(b);
            ResourceLocation socket = MultipartNetRlCodec.readRl(b);
            ResourceLocation child = null;
            if (b.readBoolean()) {
                child = MultipartNetRlCodec.readRl(b);
            }
            sink.onSocketAssign(parent, socket, child);
        }
        return MultipartDecodeStatus.OK;
    }
}
