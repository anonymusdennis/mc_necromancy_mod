package com.sirolf2009.necromancy.multipart.network.binary;

import com.sirolf2009.necromancy.multipart.math.PartTransform;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Reusable decode / encode scratch (single-threaded caller). Keeps a growable {@link FriendlyByteBuf} and math temps to
 * avoid per-field allocations in hot replication loops.
 */
public final class MultipartReplicationScratch {

    private final FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer(256, 1 << 20));
    private final PartTransform transformScratch = new PartTransform();
    private final Quaternionf quatScratch = new Quaternionf();
    private final Vector3f vecScratch = new Vector3f();

    public FriendlyByteBuf buf() {
        return buf;
    }

    public void resetBuf() {
        buf.clear();
    }

    public PartTransform transformScratch() {
        return transformScratch;
    }

    public Quaternionf quatScratch() {
        return quatScratch;
    }

    public Vector3f vecScratch() {
        return vecScratch;
    }

    public byte[] copyBufToArray() {
        byte[] out = new byte[buf.writerIndex()];
        buf.getBytes(0, out);
        return out;
    }

    public void wrapRead(byte[] blob) {
        buf.clear();
        buf.writeBytes(blob);
        buf.readerIndex(0);
    }

    public void readerAtPayloadStart(int payloadStart) {
        buf.readerIndex(payloadStart);
    }
}
