package com.sirolf2009.necromancy.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.sirolf2009.necroapi.BodyPart;
import com.sirolf2009.necroapi.BodyPartLocation;
import com.sirolf2009.necromancy.entity.EntityMinion;

@SideOnly(Side.CLIENT)
public class ModelMinion extends ModelBase
{
    public ModelRenderer head;
    public ModelRenderer torso;
    public ModelRenderer armLeft;
    public ModelRenderer armRight;
    public ModelRenderer legLeft;
    public ModelRenderer legRight;

    public ModelMinion()
    {
        textureWidth = 64;
        textureHeight = 32;
        head = new ModelRenderer(this, 0, 0);
        head.addBox(-4F, -8F, -4F, 8, 8, 8);
        head.setRotationPoint(0F, 0F, 0F);
        torso = new ModelRenderer(this, 16, 16);
        torso.addBox(-4F, 0F, -2F, 8, 12, 4);
        torso.setRotationPoint(0F, 0F, 0F);
        armLeft = new ModelRenderer(this, 40, 16);
        armLeft.addBox(-3F, -2F, -2F, 4, 12, 4);
        armLeft.setRotationPoint(5F, 2F, 0F);
        armRight = new ModelRenderer(this, 40, 16);
        armRight.addBox(-1F, -2F, -2F, 4, 12, 4);
        armRight.setRotationPoint(-5F, 2F, 0F);
        legLeft = new ModelRenderer(this, 0, 16);
        legLeft.addBox(-2F, 0F, -2F, 4, 12, 4);
        legLeft.setRotationPoint(2F, 12F, 0F);
        legRight = new ModelRenderer(this, 0, 16);
        legRight.addBox(-2F, 0F, -2F, 4, 12, 4);
        legRight.setRotationPoint(-2F, 12F, 0F);
    }

    public void updateModel(EntityMinion minion, boolean full)
    {
        BodyPart[][] parts = minion.getBodyParts();
        if (parts == null) return;

        if (full && parts[0] != null && parts[0].length > 0 && parts[0][0] != null)
        {
            BodyPart headPart = parts[0][0];
            head = headPart;
        }
        if (full && parts[1] != null && parts[1].length > 0 && parts[1][0] != null)
        {
            BodyPart torsoPart = parts[1][0];
            torso = torsoPart;
            if (torsoPart.connArmLeft != null)
            {
                armLeft.setRotationPoint(torsoPart.connArmLeft[0], torsoPart.connArmLeft[1], torsoPart.connArmLeft[2]);
            }
            if (torsoPart.connArmRight != null)
            {
                armRight.setRotationPoint(torsoPart.connArmRight[0], torsoPart.connArmRight[1], torsoPart.connArmRight[2]);
            }
            if (torsoPart.connHead != null)
            {
                head.setRotationPoint(torsoPart.connHead[0], torsoPart.connHead[1], torsoPart.connHead[2]);
            }
        }
        if (full && parts[2] != null && parts[2].length > 0 && parts[2][0] != null)
            armLeft = parts[2][0];
        if (full && parts[3] != null && parts[3].length > 0 && parts[3][0] != null)
            armRight = parts[3][0];
        if (full && parts[4] != null && parts[4].length > 0 && parts[4][0] != null)
            legLeft = parts[4][0];
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
            float netHeadYaw, float headPitch, float scale)
    {
        setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entity);
        head.render(scale);
        torso.render(scale);
        armLeft.render(scale);
        armRight.render(scale);
        legLeft.render(scale);
        legRight.render(scale);
    }
}
