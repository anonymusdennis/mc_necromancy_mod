package com.sirolf2009.necromancy.bodypart;

import net.minecraft.world.phys.AABB;

/** Immutable runtime bodypart definition resolved from disk JSON. */
public record BodypartDefinition(
    net.minecraft.resources.ResourceLocation id,
    boolean validated,
    double hbOx,
    double hbOy,
    double hbOz,
    double hbSx,
    double hbSy,
    double hbSz,
    double visDx,
    double visDy,
    double visDz,
    double visRotYawDeg,
    double visRotPitchDeg,
    double visRotRollDeg,
    BodypartFlagsJson flags,
    java.util.List<BodypartAttachmentJson> attachments
) {
    public AABB localHitbox() {
        double hx = hbSx * 0.5;
        double hy = hbSy * 0.5;
        double hz = hbSz * 0.5;
        return new AABB(hbOx - hx, hbOy - hy, hbOz - hz, hbOx + hx, hbOy + hy, hbOz + hz);
    }

    public static BodypartDefinition fromJson(BodypartDefinitionJson json) {
        var rl = net.minecraft.resources.ResourceLocation.parse(json.id);
        var hb = json.hitbox != null ? json.hitbox : new BodypartHitboxJson();
        var vo = json.visualOffset != null ? json.visualOffset : new BodypartVisualOffsetJson();
        var fg = json.flags != null ? json.flags.copy() : new BodypartFlagsJson();
        java.util.List<BodypartAttachmentJson> at;
        if (json.attachments == null || json.attachments.isEmpty()) {
            at = java.util.List.of();
        } else {
            var list = new java.util.ArrayList<BodypartAttachmentJson>();
            for (BodypartAttachmentJson a : json.attachments) {
                if (a != null) list.add(a.copy());
            }
            at = java.util.List.copyOf(list);
        }
        boolean validatedEffective = json.validated != null ? json.validated : true;
        return new BodypartDefinition(rl, validatedEffective,
            hb.ox, hb.oy, hb.oz, hb.sx, hb.sy, hb.sz,
            vo.dx, vo.dy, vo.dz,
            vo.rotYawDeg, vo.rotPitchDeg, vo.rotRollDeg,
            fg, at);
    }

    public BodypartDefinitionJson toJson() {
        BodypartDefinitionJson j = new BodypartDefinitionJson();
        j.id = id.toString();
        j.validated = validated;
        j.hitbox = new BodypartHitboxJson(hbOx, hbOy, hbOz, hbSx, hbSy, hbSz);
        j.visualOffset = new BodypartVisualOffsetJson(visDx, visDy, visDz, visRotYawDeg, visRotPitchDeg, visRotRollDeg);
        j.flags = flags.copy();
        j.attachments = new java.util.ArrayList<>(attachments);
        return j;
    }
}
