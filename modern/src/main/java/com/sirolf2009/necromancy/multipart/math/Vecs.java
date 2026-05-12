package com.sirolf2009.necromancy.multipart.math;

import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public final class Vecs {

    private Vecs() {}

    public static void copyFromVec3(Vector3f out, Vec3 v) {
        out.set((float) v.x, (float) v.y, (float) v.z);
    }

    public static Vec3 toVec3(Vector3f v) {
        return new Vec3(v.x, v.y, v.z);
    }
}
