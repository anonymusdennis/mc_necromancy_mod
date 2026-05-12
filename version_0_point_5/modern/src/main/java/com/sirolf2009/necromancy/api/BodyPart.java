package com.sirolf2009.necromancy.api;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.resources.ResourceLocation;

/**
 * Definition of a single body part owned by a {@link NecroEntityBase}.
 *
 * <p>The legacy 1.7.10 mod defined {@code BodyPart extends ModelRenderer} so that
 * the renderer could simply call {@code render()} on each part.  In 1.21.1 the
 * renderer no longer mutates state directly: {@code ModelPart}s are baked from
 * an {@link net.minecraft.client.model.geom.builders.LayerDefinition LayerDefinition}.
 * To keep the public contract stable for the 17 adapter classes, we describe a
 * part declaratively here and {@code ModelMinion} bakes the parts at render
 * time (see {@code com.sirolf2009.necromancy.client.model.ModelMinion}).
 *
 * <p>A {@link BodyPart} carries:
 * <ul>
 *     <li>the {@link CubeListBuilder} describing the geometry (cubes + UVs),</li>
 *     <li>the {@link PartPose} initial pose,</li>
 *     <li>positional metadata (where torsos / heads / arms attach),</li>
 *     <li>a back-reference to its {@link NecroEntityBase} for texture lookup.</li>
 * </ul>
 *
 * <p>The four constructor arity overloads are preserved so adapter code that
 * was lifted line-for-line from the original mod can still instantiate parts
 * with the same shape.
 */
public class BodyPart {

    /** Display / logical name for this part (defaults to the mob name). */
    public String name;
    /** Owning entity definition (texture, scaling, attribute info). */
    public NecroEntityBase entity;

    /** The geometry of this single part (one or more textured cubes). */
    public final CubeListBuilder cubes;
    /** The starting pose (translation + rotation) for the part. */
    public PartPose pose = PartPose.ZERO;

    /** Texture origin in pixels (UV offset). */
    public final int textureOffsetX;
    public final int textureOffsetY;

    // -- positional metadata used by the assembler ------------------------
    public float[] torsoPos;
    public float[] armLeftPos;
    public float[] armRightPos;
    public float[] headPos;

    /**
     * Mirrors UV mapping along the X axis (legacy {@code ModelRenderer.mirror}).
     * Set on left-side limbs so they reuse the right-side texture flipped.
     */
    public boolean mirror;

    /**
     * Base constructor used for arm/head parts that do not need attachment
     * metadata for siblings.
     */
    public BodyPart(NecroEntityBase base, int textureXOffset, int textureYOffset) {
        this.entity         = base;
        this.name           = base.mobName;
        this.textureOffsetX = textureXOffset;
        this.textureOffsetY = textureYOffset;
        this.cubes          = CubeListBuilder.create().texOffs(textureXOffset, textureYOffset);
    }

    /** Constructor used when defining legs that anchor to a torso position. */
    public BodyPart(NecroEntityBase base, float[] torsoPos, int textureXOffset, int textureYOffset) {
        this(base, textureXOffset, textureYOffset);
        this.torsoPos = torsoPos;
    }

    /**
     * Constructor used when defining a torso, which carries the attachment
     * positions of its arms and head as siblings.
     */
    public BodyPart(NecroEntityBase base,
                    float[] armLeftPos, float[] armRightPos, float[] headPos,
                    int textureXOffset, int textureYOffset) {
        this(base, textureXOffset, textureYOffset);
        this.armLeftPos  = armLeftPos;
        this.armRightPos = armRightPos;
        this.headPos     = headPos;
    }

    // ------------------------------------------------------------------ --

    /**
     * Convenience: add an axis-aligned textured cuboid in the same units the
     * legacy {@code ModelRenderer.addBox} used (pixels, anchor at origin).
     */
    public BodyPart addBox(float x, float y, float z, int sizeX, int sizeY, int sizeZ) {
        cubes.addBox(x, y, z, sizeX, sizeY, sizeZ);
        return this;
    }

    public BodyPart addBox(float x, float y, float z, int sizeX, int sizeY, int sizeZ, float scale) {
        cubes.addBox(x, y, z, sizeX, sizeY, sizeZ,
            new net.minecraft.client.model.geom.builders.CubeDeformation(scale));
        return this;
    }

    /** Pose this part at the given translation. */
    public BodyPart setPos(float x, float y, float z) {
        pose = PartPose.offsetAndRotation(x, y, z,
            pose.xRot, pose.yRot, pose.zRot);
        return this;
    }

    /** Pose this part with the given Euler rotations (radians). */
    public BodyPart setRotation(float rx, float ry, float rz) {
        pose = PartPose.offsetAndRotation(pose.x, pose.y, pose.z, rx, ry, rz);
        return this;
    }

    /** Mark this part as mirrored along X (legacy {@code ModelRenderer.mirror}). */
    public BodyPart mirror() {
        this.mirror = true;
        return this;
    }

    /** Resolves the texture used by this part (falls back to mob default). */
    public ResourceLocation texture() {
        return entity.texture;
    }
}
