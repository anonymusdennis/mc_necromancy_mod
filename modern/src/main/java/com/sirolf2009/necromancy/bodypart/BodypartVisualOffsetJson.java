package com.sirolf2009.necromancy.bodypart;

/** Part-local visual offset in blocks (same units as hitbox); does not affect collision. */
@SuppressWarnings("unused")
public final class BodypartVisualOffsetJson {

    public double dx;
    public double dy;
    public double dz;

    /** Optional Euler rotation in degrees applied to the model mesh (yaw = Y-axis, pitch = X-axis, roll = Z-axis). */
    public double rotYawDeg;
    public double rotPitchDeg;
    public double rotRollDeg;

    public BodypartVisualOffsetJson() {}

    public BodypartVisualOffsetJson(double dx, double dy, double dz) {
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
    }

    public BodypartVisualOffsetJson(double dx, double dy, double dz,
                                    double rotYawDeg, double rotPitchDeg, double rotRollDeg) {
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
        this.rotYawDeg = rotYawDeg;
        this.rotPitchDeg = rotPitchDeg;
        this.rotRollDeg = rotRollDeg;
    }

    public BodypartVisualOffsetJson copy() {
        return new BodypartVisualOffsetJson(dx, dy, dz, rotYawDeg, rotPitchDeg, rotRollDeg);
    }
}
