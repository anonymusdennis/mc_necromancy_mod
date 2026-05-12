package com.sirolf2009.necromancy.multipart.network.binary;

import com.sirolf2009.necromancy.multipart.damage.PartFunctionalFlag;
import net.minecraft.network.FriendlyByteBuf;

import java.util.EnumSet;
import java.util.List;

/**
 * Sparse damage / sever / flags keyed by canonical sorted part index (same ordering as transform deltas).
 */
public final class MultipartDamageDeltaBinary {

    public static final byte OP_DAMAGE_V1 = 1;

    public static final int MASK_HAS_HP_CUR = 1;
    public static final int MASK_HAS_HP_MAX = 2;
    public static final int MASK_HAS_FLAGS = 4;

    public interface Sink {
        void onDamageState(int sortedPartIndex, float curHp, float maxHp, int functionalFlagBits,
                           boolean severed, boolean destroyed, int mask);
    }

    private MultipartDamageDeltaBinary() {}

    public static byte[] encodeV1(int topologyRevisionGuard, int flags, List<DamageOp> ops, MultipartReplicationScratch scratch) {
        FriendlyByteBuf b = scratch.buf();
        scratch.resetBuf();
        boolean crc = (flags & MultipartPacketFlags.HAS_CRC32) != 0;
        boolean half = (flags & MultipartPacketFlags.HALF_PRECISION_TRS) != 0;

        b.writeByte(MultipartSchemaVersions.DAMAGE_SYNC_V1);
        b.writeShortLE(flags & 0xFFFF);
        int crcSlot = -1;
        if (crc) {
            crcSlot = b.writerIndex();
            b.writeIntLE(0);
        }

        b.writeVarInt(topologyRevisionGuard);
        b.writeVarInt(ops.size());

        for (DamageOp op : ops) {
            b.writeByte(OP_DAMAGE_V1);
            b.writeVarInt(op.sortedPartIndex());
            b.writeByte(op.mask());
            if ((op.mask() & MASK_HAS_HP_CUR) != 0) {
                writeF(b, op.curHp(), half);
            }
            if ((op.mask() & MASK_HAS_HP_MAX) != 0) {
                writeF(b, op.maxHp(), half);
            }
            if ((op.mask() & MASK_HAS_FLAGS) != 0) {
                b.writeVarInt(op.functionalFlagBits());
            }
            b.writeBoolean(op.severed());
            b.writeBoolean(op.destroyed());
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

    /** HP fields optional via mask; severed/destroyed booleans always present on wire (tiny cost). */
    public record DamageOp(int sortedPartIndex, int mask, float curHp, float maxHp, int functionalFlagBits,
                           boolean severed, boolean destroyed) {}

    public static int packFunctionalFlags(EnumSet<PartFunctionalFlag> flags) {
        int bits = 0;
        for (PartFunctionalFlag f : flags) {
            bits |= 1 << f.ordinal();
        }
        return bits;
    }

    public static EnumSet<PartFunctionalFlag> unpackFunctionalFlags(int bits) {
        EnumSet<PartFunctionalFlag> out = EnumSet.noneOf(PartFunctionalFlag.class);
        for (PartFunctionalFlag f : PartFunctionalFlag.values()) {
            if ((bits & (1 << f.ordinal())) != 0) {
                out.add(f);
            }
        }
        return out;
    }

    public static MultipartDecodeStatus decodeV1(byte[] blob, int clientTopologyRevision, Sink sink, MultipartReplicationScratch scratch) {
        if (blob == null || blob.length < MultipartReplicationHeader.PAYLOAD_START_NO_CRC) {
            return MultipartDecodeStatus.TRUNCATED;
        }
        scratch.wrapRead(blob);
        FriendlyByteBuf b = scratch.buf();
        if (b.readByte() != MultipartSchemaVersions.DAMAGE_SYNC_V1) {
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
        boolean half = (flags & MultipartPacketFlags.HALF_PRECISION_TRS) != 0;
        for (int i = 0; i < count; i++) {
            if (!b.isReadable()) {
                return MultipartDecodeStatus.TRUNCATED;
            }
            if (b.readByte() != OP_DAMAGE_V1) {
                return MultipartDecodeStatus.ILLEGAL_OPCODE;
            }
            int idx = b.readVarInt();
            int mask = b.readUnsignedByte();
            float cur = 0f;
            float max = 0f;
            if ((mask & MASK_HAS_HP_CUR) != 0) {
                cur = readF(b, half);
            }
            if ((mask & MASK_HAS_HP_MAX) != 0) {
                max = readF(b, half);
            }
            int flagBits = 0;
            if ((mask & MASK_HAS_FLAGS) != 0) {
                flagBits = b.readVarInt();
            }
            boolean severed = b.readBoolean();
            boolean destroyed = b.readBoolean();
            sink.onDamageState(idx, cur, max, flagBits, severed, destroyed, mask);
        }
        return MultipartDecodeStatus.OK;
    }

    private static void writeF(FriendlyByteBuf b, float v, boolean half) {
        if (half) {
            b.writeShort(Float.floatToFloat16(v));
        } else {
            b.writeFloat(v);
        }
    }

    private static float readF(FriendlyByteBuf b, boolean half) {
        return half ? Float.float16ToFloat(b.readShort()) : b.readFloat();
    }
}
