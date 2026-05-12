package com.sirolf2009.necromancy.multipart.network.binary;

import com.sirolf2009.necromancy.multipart.damage.PartFunctionalFlag;
import com.sirolf2009.necromancy.multipart.math.PartTransform;
import net.minecraft.resources.ResourceLocation;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MultipartReplicationBinaryTest {

    private static PartTransform samplePose() {
        PartTransform p = new PartTransform();
        p.setTranslation(new Vector3f(0.25f, -1.5f, 0.125f));
        p.setRotation(new Quaternionf().rotateY((float) Math.PI / 7f).normalize());
        p.setScale(new Vector3f(0.9f, 1.1f, 1f));
        return p;
    }

    @Test
    void transformDeltaRoundTripFullPrecision() {
        MultipartReplicationScratch scratch = new MultipartReplicationScratch();
        int topo = 42;
        int flags = 0;
        List<MultipartTransformDeltaBinary.SimLocalOp> locals = List.of(
            new MultipartTransformDeltaBinary.SimLocalOp(0, samplePose()),
            new MultipartTransformDeltaBinary.SimLocalOp(3, samplePose()));
        List<MultipartTransformDeltaBinary.AttachedOp> attached = List.of(
            new MultipartTransformDeltaBinary.AttachedOp(1, false),
            new MultipartTransformDeltaBinary.AttachedOp(2, true));

        byte[] blob = MultipartTransformDeltaBinary.encodeV1(topo, flags, locals, attached, scratch);

        AtomicInteger localHits = new AtomicInteger();
        AtomicInteger attachHits = new AtomicInteger();
        MultipartDecodeStatus st = MultipartTransformDeltaBinary.decodeV1(blob, topo,
            new MultipartTransformDeltaBinary.Sink() {
                @Override
                public void onSimLocal(int sortedPartIndex, PartTransform pose) {
                    localHits.incrementAndGet();
                    assertEquals(0.25f, pose.translation().x, 1e-5f);
                }

                @Override
                public void onAttached(int sortedPartIndex, boolean attachedToParent) {
                    attachHits.incrementAndGet();
                }
            }, scratch);

        assertEquals(MultipartDecodeStatus.OK, st);
        assertEquals(2, localHits.get());
        assertEquals(2, attachHits.get());
        assertEquals(MultipartDecodeStatus.OK, MultipartReplicationValidate.preflightTransformV1(blob, topo));
        assertTrue(MultipartReplicationValidate.summarize(blob).contains("flags=0x0000"));
    }

    @Test
    void transformDeltaHalfPrecisionAndTrailingExtensionIgnored() {
        MultipartReplicationScratch scratch = new MultipartReplicationScratch();
        int topo = 7;
        int flags = MultipartPacketFlags.HALF_PRECISION_TRS;
        byte[] blob = MultipartTransformDeltaBinary.encodeV1(topo, flags,
            List.of(new MultipartTransformDeltaBinary.SimLocalOp(2, samplePose())),
            List.of(), scratch);

        byte[] extended = new byte[blob.length + 4];
        System.arraycopy(blob, 0, extended, 0, blob.length);
        extended[blob.length] = (byte) 0xff;

        AtomicInteger ops = new AtomicInteger();
        assertEquals(MultipartDecodeStatus.OK,
            MultipartTransformDeltaBinary.decodeV1(extended, topo, new MultipartTransformDeltaBinary.Sink() {
                @Override
                public void onSimLocal(int sortedPartIndex, PartTransform pose) {
                    ops.incrementAndGet();
                }

                @Override
                public void onAttached(int sortedPartIndex, boolean attachedToParent) {
                }
            }, scratch));
        assertEquals(1, ops.get());
    }

    @Test
    void transformDeltaTopologyMismatchAndBadCrc() {
        MultipartReplicationScratch scratch = new MultipartReplicationScratch();
        int flags = MultipartPacketFlags.HAS_CRC32;
        byte[] blob = MultipartTransformDeltaBinary.encodeV1(99, flags,
            List.of(new MultipartTransformDeltaBinary.SimLocalOp(0, samplePose())),
            List.of(), scratch);

        assertEquals(MultipartDecodeStatus.TOPOLOGY_MISMATCH,
            MultipartTransformDeltaBinary.decodeV1(blob, 98, noopTransformSink(), scratch));

        blob[blob.length - 1] ^= (byte) 0x80;
        assertEquals(MultipartDecodeStatus.BAD_CRC,
            MultipartTransformDeltaBinary.decodeV1(blob, 99, noopTransformSink(), scratch));
    }

    @Test
    void damageDeltaRoundTripPackedFlags() {
        MultipartReplicationScratch scratch = new MultipartReplicationScratch();
        int topo = 11;
        EnumSet<PartFunctionalFlag> fs = EnumSet.of(PartFunctionalFlag.DISABLED, PartFunctionalFlag.NO_COLLISION);
        int bits = MultipartDamageDeltaBinary.packFunctionalFlags(fs);
        List<MultipartDamageDeltaBinary.DamageOp> ops = List.of(
            new MultipartDamageDeltaBinary.DamageOp(0,
                MultipartDamageDeltaBinary.MASK_HAS_HP_CUR | MultipartDamageDeltaBinary.MASK_HAS_HP_MAX | MultipartDamageDeltaBinary.MASK_HAS_FLAGS,
                3.5f, 10f, bits, true, false));

        byte[] blob = MultipartDamageDeltaBinary.encodeV1(topo, 0, ops, scratch);

        AtomicInteger hits = new AtomicInteger();
        MultipartDecodeStatus st = MultipartDamageDeltaBinary.decodeV1(blob, topo,
            (idx, cur, max, flagBits, severed, destroyed, mask) -> {
                hits.incrementAndGet();
                assertEquals(3.5f, cur, 1e-5f);
                assertEquals(10f, max, 1e-5f);
                assertEquals(bits, flagBits);
                assertTrue(severed);
                assertEquals(false, destroyed);
            }, scratch);

        assertEquals(MultipartDecodeStatus.OK, st);
        assertEquals(1, hits.get());
        assertEquals(fs, MultipartDamageDeltaBinary.unpackFunctionalFlags(bits));
        assertEquals(MultipartDecodeStatus.OK, MultipartReplicationValidate.preflightDamageV1(blob, topo));
    }

    @Test
    void topologyDeltaAssignAndClearRoundTrip() {
        MultipartReplicationScratch scratch = new MultipartReplicationScratch();
        ResourceLocation parent = ResourceLocation.parse("necromancy:torso");
        ResourceLocation socket = ResourceLocation.parse("necromancy:socket_arm_r");
        ResourceLocation child = ResourceLocation.parse("necromancy:forearm_r");

        int topo = 3;
        byte[] blob = MultipartTopologyDeltaBinary.encodeV1(topo, 0, List.of(
            new MultipartTopologyDeltaBinary.SocketAssignOp(parent, socket, child)), scratch);

        ArrayList<String> trace = new ArrayList<>();
        assertEquals(MultipartDecodeStatus.OK, MultipartTopologyDeltaBinary.decodeV1(blob, topo,
            (p, s, c) -> trace.add(p + "|" + s + "|" + c), scratch));
        assertEquals(1, trace.size());
        assertEquals(parent.toString() + "|" + socket.toString() + "|" + child.toString(), trace.get(0));

        byte[] clearBlob = MultipartTopologyDeltaBinary.encodeV1(topo, 0, List.of(
            new MultipartTopologyDeltaBinary.SocketAssignOp(parent, socket, null)), scratch);

        trace.clear();
        assertEquals(MultipartDecodeStatus.OK, MultipartTopologyDeltaBinary.decodeV1(clearBlob, topo,
            (p, s, c) -> trace.add(String.valueOf(c)), scratch));
        assertEquals("null", trace.get(0));
        assertEquals(MultipartDecodeStatus.OK, MultipartReplicationValidate.preflightTopologyV1(blob, topo));
    }

    @Test
    void summarizeIncludesHeaderFieldsWhenCrcPresent() {
        MultipartReplicationScratch scratch = new MultipartReplicationScratch();
        byte[] blob = MultipartTopologyDeltaBinary.encodeV1(1,
            MultipartPacketFlags.HAS_CRC32 | MultipartPacketFlags.HALF_PRECISION_TRS,
            List.of(), scratch);
        String s = MultipartReplicationValidate.summarize(blob);
        assertTrue(s.contains("crc=0x"));
        assertTrue(s.contains("half_trs=true"));
    }

    private static MultipartTransformDeltaBinary.Sink noopTransformSink() {
        return new MultipartTransformDeltaBinary.Sink() {
            @Override
            public void onSimLocal(int sortedPartIndex, PartTransform pose) {
            }

            @Override
            public void onAttached(int sortedPartIndex, boolean attachedToParent) {
            }
        };
    }
}
