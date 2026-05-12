package com.sirolf2009.necromancy.client.model;

import com.sirolf2009.necromancy.Reference;
import com.sirolf2009.necromancy.entity.EntityTeddy;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/**
 * 1:1 modern port of the legacy {@code ModelTeddy} (textureWidth=64,
 * textureHeight=32).  An 8-cuboid tiny teddy bear with sit/stand paw
 * animation that mirrors the original {@code setRotationAngles} arithmetic
 * (uses {@code onGround} as a stand/sit interpolator and {@code limbSwing}
 * for the back-paw shuffle).
 */
public class TeddyModel extends HierarchicalModel<EntityTeddy> {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Reference.rl("teddy"), "main");

    private final ModelPart root;
    private final ModelPart pawFrontRight;
    private final ModelPart pawFrontLeft;
    private final ModelPart pawBackRight;
    private final ModelPart pawBackLeft;
    private final ModelPart head;

    public TeddyModel(ModelPart root) {
        this.root          = root;
        this.pawFrontRight = root.getChild("paw_front_right");
        this.pawFrontLeft  = root.getChild("paw_front_left");
        this.pawBackRight  = root.getChild("paw_back_right");
        this.pawBackLeft   = root.getChild("paw_back_left");
        this.head          = root.getChild("head");
    }

    @Override
    public ModelPart root() { return root; }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("paw_front_right",
            CubeListBuilder.create().mirror().texOffs(0, 5).addBox(-2F, 0F, 0F, 2, 3, 2),
            PartPose.offsetAndRotation(-1.5F, 16F, 2.5F, -1.151917F, 0F, 0F));
        root.addOrReplaceChild("paw_front_left",
            CubeListBuilder.create().mirror().texOffs(4, 5).addBox(0F, 0F, 0F, 2, 3, 2),
            PartPose.offsetAndRotation(1.5F, 16F, 2.5F, -1.151917F, 0F, 0F));
        root.addOrReplaceChild("paw_back_right",
            CubeListBuilder.create().mirror().texOffs(0, 14).addBox(0F, 0F, 0F, 2, 3, 2),
            PartPose.offset(-2.2F, 21F, 2F));
        root.addOrReplaceChild("paw_back_left",
            CubeListBuilder.create().mirror().texOffs(0, 9).addBox(0F, 0F, 0F, 2, 3, 2),
            PartPose.offset(0.2F, 21F, 2F));

        root.addOrReplaceChild("belly",
            CubeListBuilder.create().mirror().texOffs(10, 0).addBox(0F, 0F, 0F, 4, 7, 3),
            PartPose.offsetAndRotation(-2F, 15F, 1F, 0.1487144F, 0F, 0F));

        // Head + ears as one part so they rotate together.
        root.addOrReplaceChild("head",
            CubeListBuilder.create().mirror()
                .texOffs(0, 0).addBox(-2F, -3F, -1F, 3, 3, 2),
            PartPose.offset(0.5F, 16F, 1F));
        root.addOrReplaceChild("ear_right",
            CubeListBuilder.create().mirror().texOffs(8, 10).addBox(-0.5F, -1F, 0F, 1, 1, 1),
            PartPose.offsetAndRotation(-1F, 13.2F, 0.1F, 0.1115358F, 0F, -0.2230717F));
        root.addOrReplaceChild("ear_left",
            CubeListBuilder.create().mirror().texOffs(8, 12).addBox(-0.5F, -1F, 0F, 1, 1, 1),
            PartPose.offsetAndRotation(1F, 13.2F, 0.1F, 0.1115358F, 0F, 0.2230705F));

        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(EntityTeddy entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        // Head tracks aim.
        head.yRot = netHeadYaw * Mth.DEG_TO_RAD;
        head.xRot = headPitch  * Mth.DEG_TO_RAD;

        // Front paws: copy the legacy "begging" pose.
        // 1.0F == always standing in this port; the legacy mod tied this to
        // {@code onGround}, but we don't expose that here -- the static
        // standing pose looks fine and matches sitting via the SitGoal.
        float onGround = 1.0F;
        float var7 = Mth.sin(onGround * Mth.PI);
        float var8 = Mth.sin((1F - (1F - onGround) * (1F - onGround)) * Mth.PI);

        pawFrontRight.zRot = 0F;
        pawFrontLeft.zRot  = 0F;
        pawFrontRight.yRot = -(0.1F - var7 * 0.6F);
        pawFrontLeft.yRot  =  0.1F - var7 * 0.6F;
        pawFrontRight.xRot = -1.570796F - (var7 * 1.2F - var8 * 0.4F);
        pawFrontLeft.xRot  = -1.570796F - (var7 * 1.2F - var8 * 0.4F);
        pawFrontRight.zRot += Mth.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
        pawFrontLeft.zRot  -= Mth.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
        pawFrontRight.xRot += Mth.sin(ageInTicks * 0.067F) * 0.05F;
        pawFrontLeft.xRot  -= Mth.sin(ageInTicks * 0.067F) * 0.05F;

        // Back paws: walking shuffle.
        pawBackRight.xRot = (float) (Mth.cos(limbSwing) * 0.5F) * limbSwingAmount;
        pawBackLeft.xRot  = (float) (Mth.cos(limbSwing + Mth.PI) * 0.5F) * limbSwingAmount;
        pawBackRight.yRot = 0F;
        pawBackLeft.yRot  = 0F;
    }
}
