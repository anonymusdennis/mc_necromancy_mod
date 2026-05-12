package com.sirolf2009.necromancy.multipart.network.binary;

import com.sirolf2009.necromancy.multipart.damage.PartFunctionalFlag;
import com.sirolf2009.necromancy.multipart.math.PartTransform;
import net.minecraft.resources.ResourceLocation;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Micro-benchmarks for codec hot paths (opt-in: {@code -Dnecromancy.serializationBench=true}).
 * Prints timings to stdout; does not enforce thresholds (machine-dependent).
 */
@Tag("serializationBench")
class MultipartReplicationSerializationBenchTest {

    private static final int WARMUP = 3_000;
    private static final int ITERS = 25_000;

    @Test
    @EnabledIfSystemProperty(named = "necromancy.serializationBench", matches = "true")
    void benchTransformEncodeDecodeReuseScratch() {
        MultipartReplicationScratch scratch = new MultipartReplicationScratch();
        PartTransform pose = new PartTransform();
        pose.setTranslation(new Vector3f(0.1f, 0.2f, 0.3f));
        pose.setRotation(new Quaternionf().rotateXYZ(0.4f, 0.5f, 0.6f).normalize());
        pose.setScale(new Vector3f(1f, 1f, 1f));

        int topo = 42;
        int flags = MultipartPacketFlags.HALF_PRECISION_TRS;
        List<MultipartTransformDeltaBinary.SimLocalOp> locals = List.of(
            new MultipartTransformDeltaBinary.SimLocalOp(0, pose),
            new MultipartTransformDeltaBinary.SimLocalOp(5, pose));
        List<MultipartTransformDeltaBinary.AttachedOp> attached = List.of(
            new MultipartTransformDeltaBinary.AttachedOp(1, true));

        for (int i = 0; i < WARMUP; i++) {
            byte[] b = MultipartTransformDeltaBinary.encodeV1(topo, flags, locals, attached, scratch);
            Assertions.assertEquals(MultipartDecodeStatus.OK,
                MultipartTransformDeltaBinary.decodeV1(b, topo, scratchSink(), scratch));
        }

        long t0 = System.nanoTime();
        int lastLen = 0;
        for (int i = 0; i < ITERS; i++) {
            byte[] b = MultipartTransformDeltaBinary.encodeV1(topo, flags, locals, attached, scratch);
            lastLen = b.length;
            MultipartDecodeStatus st = MultipartTransformDeltaBinary.decodeV1(b, topo, scratchSink(), scratch);
            Assertions.assertEquals(MultipartDecodeStatus.OK, st);
        }
        long ns = System.nanoTime() - t0;
        double perOpMs = ns / 1_000_000.0 / ITERS;
        System.out.printf("[multipart bench] transform FP16 encode+decode scratch-reused: %d iters, %.4f ms/op, last blob %d bytes%n",
            ITERS, perOpMs, lastLen);
    }

    @Test
    @EnabledIfSystemProperty(named = "necromancy.serializationBench", matches = "true")
    void benchDamageEncodeDecode() {
        MultipartReplicationScratch scratch = new MultipartReplicationScratch();
        int topo = 9;
        var ops = List.of(
            new MultipartDamageDeltaBinary.DamageOp(0,
                MultipartDamageDeltaBinary.MASK_HAS_HP_CUR | MultipartDamageDeltaBinary.MASK_HAS_FLAGS,
                4f, 0f, 1 << PartFunctionalFlag.DISABLED.ordinal(), false, false));

        for (int i = 0; i < WARMUP; i++) {
            byte[] b = MultipartDamageDeltaBinary.encodeV1(topo, 0, ops, scratch);
            MultipartDamageDeltaBinary.decodeV1(b, topo, (a, b1, c, d, e, f, g) -> {}, scratch);
        }

        long t0 = System.nanoTime();
        int lastLen = 0;
        for (int i = 0; i < ITERS; i++) {
            byte[] b = MultipartDamageDeltaBinary.encodeV1(topo, 0, ops, scratch);
            lastLen = b.length;
            Assertions.assertEquals(MultipartDecodeStatus.OK,
                MultipartDamageDeltaBinary.decodeV1(b, topo, (a, b1, c, d, e, f, g) -> {}, scratch));
        }
        long ns = System.nanoTime() - t0;
        double perOpMs = ns / 1_000_000.0 / ITERS;
        System.out.printf("[multipart bench] damage encode+decode: %d iters, %.4f ms/op, last blob %d bytes%n",
            ITERS, perOpMs, lastLen);
    }

    @Test
    @EnabledIfSystemProperty(named = "necromancy.serializationBench", matches = "true")
    void benchTopologyEncodeDecode() {
        MultipartReplicationScratch scratch = new MultipartReplicationScratch();
        ResourceLocation p = ResourceLocation.parse("necromancy:torso");
        ResourceLocation s = ResourceLocation.parse("necromancy:socket");
        ResourceLocation c = ResourceLocation.parse("necromancy:limb");
        var ops = new ArrayList<MultipartTopologyDeltaBinary.SocketAssignOp>();
        ops.add(new MultipartTopologyDeltaBinary.SocketAssignOp(p, s, c));

        int topo = 2;
        for (int i = 0; i < WARMUP; i++) {
            byte[] b = MultipartTopologyDeltaBinary.encodeV1(topo, 0, ops, scratch);
            MultipartTopologyDeltaBinary.decodeV1(b, topo, (x, y, z) -> {}, scratch);
        }

        long t0 = System.nanoTime();
        int lastLen = 0;
        for (int i = 0; i < ITERS; i++) {
            byte[] b = MultipartTopologyDeltaBinary.encodeV1(topo, 0, ops, scratch);
            lastLen = b.length;
            Assertions.assertEquals(MultipartDecodeStatus.OK,
                MultipartTopologyDeltaBinary.decodeV1(b, topo, (x, y, z) -> {}, scratch));
        }
        long ns = System.nanoTime() - t0;
        double perOpMs = ns / 1_000_000.0 / ITERS;
        System.out.printf("[multipart bench] topology encode+decode: %d iters, %.4f ms/op, last blob %d bytes%n",
            ITERS, perOpMs, lastLen);
    }

    private static MultipartTransformDeltaBinary.Sink scratchSink() {
        return new MultipartTransformDeltaBinary.Sink() {
            @Override
            public void onSimLocal(int sortedPartIndex, PartTransform reusablePoseReadOnlyViaScratch) {
            }

            @Override
            public void onAttached(int sortedPartIndex, boolean attachedToParent) {
            }
        };
    }
}
