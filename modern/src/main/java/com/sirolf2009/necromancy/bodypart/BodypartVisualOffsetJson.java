package com.sirolf2009.necromancy.bodypart;

/** Part-local visual offset in blocks (same units as hitbox); does not affect collision. */
@SuppressWarnings("unused")
public final class BodypartVisualOffsetJson {

    public double dx;
    public double dy;
    public double dz;

    public BodypartVisualOffsetJson() {}

    public BodypartVisualOffsetJson(double dx, double dy, double dz) {
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
    }

    public BodypartVisualOffsetJson copy() {
        return new BodypartVisualOffsetJson(dx, dy, dz);
    }
}
