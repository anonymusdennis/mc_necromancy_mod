package com.sirolf2009.necromancy.bodypart;

import net.minecraft.world.phys.Vec3;

/** Shared math for bodypart preview (yaw rotation in horizontal plane). */
public final class BodypartPreviewGeom {

    private BodypartPreviewGeom() {}

    public static Vec3 rotateY(Vec3 v, float degrees) {
        float rad = (float) Math.toRadians(-degrees);
        float c = (float) Math.cos(rad);
        float s = (float) Math.sin(rad);
        return new Vec3(v.x * c - v.z * s, v.y, v.x * s + v.z * c);
    }
}
