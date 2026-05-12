package com.sirolf2009.necromancy.multipart.network.binary;

import com.sirolf2009.necromancy.multipart.math.PartTransform;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/** Quaternion xyzw + translation + scale — float32 or float16 lanes. */
public final class MultipartPackedTrsCodec {

    private MultipartPackedTrsCodec() {}

    private static void writeHalf(FriendlyByteBuf buf, float f) {
        buf.writeShort(Float.floatToFloat16(f));
    }

    private static float readHalf(FriendlyByteBuf buf) {
        return Float.float16ToFloat(buf.readShort());
    }

    public static void writeSimLocal(FriendlyByteBuf buf, PartTransform local, boolean halfPrecision,
                                     Quaternionf qScratch, Vector3f vScratch) {
        if (halfPrecision) {
            local.translationInto(vScratch);
            writeHalf(buf, vScratch.x);
            writeHalf(buf, vScratch.y);
            writeHalf(buf, vScratch.z);
            local.rotationInto(qScratch);
            writeHalf(buf, qScratch.x);
            writeHalf(buf, qScratch.y);
            writeHalf(buf, qScratch.z);
            writeHalf(buf, qScratch.w);
            local.scaleInto(vScratch);
            writeHalf(buf, vScratch.x);
            writeHalf(buf, vScratch.y);
            writeHalf(buf, vScratch.z);
        } else {
            local.translationInto(vScratch);
            buf.writeFloat(vScratch.x);
            buf.writeFloat(vScratch.y);
            buf.writeFloat(vScratch.z);
            local.rotationInto(qScratch);
            buf.writeFloat(qScratch.x);
            buf.writeFloat(qScratch.y);
            buf.writeFloat(qScratch.z);
            buf.writeFloat(qScratch.w);
            local.scaleInto(vScratch);
            buf.writeFloat(vScratch.x);
            buf.writeFloat(vScratch.y);
            buf.writeFloat(vScratch.z);
        }
    }

    public static void readSimLocal(FriendlyByteBuf buf, PartTransform dest, boolean halfPrecision,
                                    Quaternionf qScratch, Vector3f vScratch) {
        if (halfPrecision) {
            vScratch.set(readHalf(buf), readHalf(buf), readHalf(buf));
            dest.setTranslation(new Vec3(vScratch.x, vScratch.y, vScratch.z));
            qScratch.set(readHalf(buf), readHalf(buf), readHalf(buf), readHalf(buf)).normalize();
            dest.setRotation(qScratch);
            vScratch.set(readHalf(buf), readHalf(buf), readHalf(buf));
            dest.setScale(vScratch);
        } else {
            vScratch.set(buf.readFloat(), buf.readFloat(), buf.readFloat());
            dest.setTranslation(new Vec3(vScratch.x, vScratch.y, vScratch.z));
            qScratch.set(buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat()).normalize();
            dest.setRotation(qScratch);
            vScratch.set(buf.readFloat(), buf.readFloat(), buf.readFloat());
            dest.setScale(vScratch);
        }
    }
}
