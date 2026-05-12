package com.sirolf2009.necromancy.bodypart;

import com.sirolf2009.necromancy.multipart.math.PartTransform;
import com.sirolf2009.necromancy.multipart.math.QuaternionOps;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

/**
 * Attachment socket in part-local space. Translations {@code ox,oy,oz} are along the part's local axes before the
 * bodypart item is world-oriented; preview rendering applies the preview entity yaw, and the multipart binder composes
 * these with parent bone poses.
 */
@SuppressWarnings("unused")
public final class BodypartAttachmentJson {
    public String name = "socket";
    public double ox;
    public double oy;
    public double oz;
    public int priority;
    public boolean hasRotationPivot;
    public double pivotOx;
    public double pivotOy;
    public double pivotOz;

    /** Optional euler rotation of the socket (degrees). Used when {@link #quatW} is absent. */
    public Double eulerYawDeg;
    public Double eulerPitchDeg;
    public Double eulerRollDeg;

    /** Optional unit quaternion (w + i + j + k). When all four are non-null, overrides euler fields. */
    public Double quatX;
    public Double quatY;
    public Double quatZ;
    public Double quatW;

    public BodypartAttachmentJson() {}

    public PartTransform socketLocalTransform() {
        PartTransform t = new PartTransform();
        t.setTranslation(new Vec3(ox, oy, oz));
        if (quatW != null && quatX != null && quatY != null && quatZ != null) {
            Quaternionf q = new Quaternionf(
                quatX.floatValue(), quatY.floatValue(), quatZ.floatValue(), quatW.floatValue());
            q.normalize();
            t.setRotation(q);
            return t;
        }
        float yaw = eulerYawDeg != null ? eulerYawDeg.floatValue() : 0f;
        float pitch = eulerPitchDeg != null ? eulerPitchDeg.floatValue() : 0f;
        float roll = eulerRollDeg != null ? eulerRollDeg.floatValue() : 0f;
        t.setRotation(QuaternionOps.fromYawPitchRollDegrees(yaw, pitch, roll));
        return t;
    }

    public BodypartAttachmentJson copy() {
        BodypartAttachmentJson c = new BodypartAttachmentJson();
        c.name = name;
        c.ox = ox;
        c.oy = oy;
        c.oz = oz;
        c.priority = priority;
        c.hasRotationPivot = hasRotationPivot;
        c.pivotOx = pivotOx;
        c.pivotOy = pivotOy;
        c.pivotOz = pivotOz;
        c.eulerYawDeg = eulerYawDeg;
        c.eulerPitchDeg = eulerPitchDeg;
        c.eulerRollDeg = eulerRollDeg;
        c.quatX = quatX;
        c.quatY = quatY;
        c.quatZ = quatZ;
        c.quatW = quatW;
        return c;
    }
}
