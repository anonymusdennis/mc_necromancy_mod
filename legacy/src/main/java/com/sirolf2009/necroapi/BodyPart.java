package com.sirolf2009.necroapi;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * A ModelRenderer extension that carries a reference to its NecroEntityBase
 * owner and optional connection-point data for the minion model assembler.
 */
@SideOnly(Side.CLIENT)
public class BodyPart extends ModelRenderer
{
    public final String name;
    public final NecroEntityBase entity;

    /** Connection points used when this part is a torso: [armLeft, armRight, head]. May be null. */
    public float[] connArmLeft;
    public float[] connArmRight;
    public float[] connHead;

    /** Connection point used when this part is a leg: position relative to torso. May be null. */
    public float[] connTorso;

    /** Basic body part: entity, model, texture offset. */
    public BodyPart(NecroEntityBase entity, ModelBase model, int texOffX, int texOffY)
    {
        super(model, texOffX, texOffY);
        this.entity = entity;
        this.name = entity.mobName;
    }

    /** Leg body part: entity, torso-connection point, model, texture offset. */
    public BodyPart(NecroEntityBase entity, float[] torsoPos, ModelBase model, int texOffX, int texOffY)
    {
        super(model, texOffX, texOffY);
        this.entity = entity;
        this.name = entity.mobName;
        this.connTorso = torsoPos;
    }

    /** Torso body part: entity, arm-left pos, arm-right pos, head pos, model, texture offset. */
    public BodyPart(NecroEntityBase entity, float[] armLeftPos, float[] armRightPos, float[] headPos,
            ModelBase model, int texOffX, int texOffY)
    {
        super(model, texOffX, texOffY);
        this.entity = entity;
        this.name = entity.mobName;
        this.connArmLeft = armLeftPos;
        this.connArmRight = armRightPos;
        this.connHead = headPos;
    }
}
