package com.sirolf2009.necromancy.bodypart;

/** Gson DTO: axis-aligned box relative to the part origin (before parenting). Sizes are full extents in blocks. */
@SuppressWarnings("unused") // Gson field reflection
public final class BodypartHitboxJson {
    public double ox;
    public double oy;
    public double oz;
    /** Full width in X (not half-extent). */
    public double sx;
    public double sy;
    public double sz;

    public BodypartHitboxJson() {}

    public BodypartHitboxJson(double ox, double oy, double oz, double sx, double sy, double sz) {
        this.ox = ox;
        this.oy = oy;
        this.oz = oz;
        this.sx = sx;
        this.sy = sy;
        this.sz = sz;
    }

    public BodypartHitboxJson copy() {
        return new BodypartHitboxJson(ox, oy, oz, sx, sy, sz);
    }
}
