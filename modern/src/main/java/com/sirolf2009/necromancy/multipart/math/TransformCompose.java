package com.sirolf2009.necromancy.multipart.math;

import com.sirolf2009.necromancy.multipart.WorldPose;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Parent × local TRS composition. All operations are thread-safe when callers do not share mutable {@link WorldPose.Mutable} buffers.
 */
public final class TransformCompose {

    private TransformCompose() {}

    public static void composeInto(WorldPose parent, PartTransform local, WorldPose.Mutable dest) {
        Vec3 worldPos = composeTranslation(parent, local);
        Quaternionf worldRot = new Quaternionf();
        parent.orientationInto(worldRot);
        Quaternionf lr = new Quaternionf();
        local.rotationInto(lr);
        worldRot.mul(lr, worldRot).normalize();

        Vector3f worldScale = new Vector3f();
        parent.scaleInto(worldScale);
        Vector3f ls = new Vector3f();
        local.scaleInto(ls);
        worldScale.mul(ls);

        dest.set(worldPos, worldRot, worldScale);
    }

    public static WorldPose compose(WorldPose parent, PartTransform local) {
        WorldPose.Mutable m = new WorldPose.Mutable();
        composeInto(parent, local, m);
        return m.toImmutable();
    }

    /** Writes scaled & rotated local translation into {@code scratch}, returns world position (allocates {@link Vec3}). */
    public static Vec3 composeTranslation(WorldPose parent, PartTransform local) {
        Vector3f scratch = new Vector3f();
        local.translationInto(scratch);
        Vector3f ps = new Vector3f();
        parent.scaleInto(ps);
        scratch.mul(ps);

        Quaternionf po = new Quaternionf();
        parent.orientationInto(po);
        po.transform(scratch);

        return parent.position().add(scratch.x, scratch.y, scratch.z);
    }
}
