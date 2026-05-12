package com.sirolf2009.necromancy.client.model;

import com.sirolf2009.necromancy.Reference;
import com.sirolf2009.necromancy.entity.EntityNightCrawler;
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
 * 1:1 modern port of the legacy {@code ModelNightCrawler} (textureWidth=64,
 * textureHeight=32).  The legacy mod hardcoded the limb geometry and applied
 * a {@code GL11.glScalef(1.4F,1.4F,1.4F)} + {@code translate(0,-0.4F,0)}
 * wrapper before rendering -- we bake that into the entity render code.
 *
 * <p>The legacy model itself did not override {@code setRotationAngles}, so
 * the parts stay at their constructor poses.  We add a minimal head-yaw +
 * walking limb swing on top so the modern animation system can hook in.
 */
public class NightCrawlerModel extends HierarchicalModel<EntityNightCrawler> {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Reference.rl("nightcrawler"), "main");

    private final ModelPart root;
    private final ModelPart headset;
    private final ModelPart leftArm;
    private final ModelPart rightArm;

    public NightCrawlerModel(ModelPart root) {
        this.root      = root;
        this.headset   = root.getChild("headset");
        this.leftArm   = root.getChild("leftarmset");
        this.rightArm  = root.getChild("rightarmset");
    }

    @Override
    public ModelPart root() { return root; }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("midBody",
            CubeListBuilder.create().mirror().texOffs(49, 9).addBox(-4F, 0F, -2F, 4, 5, 3),
            PartPose.offsetAndRotation(2F, 14.46667F, -1.133333F, 0.1858931F, 0F, 0F));

        root.addOrReplaceChild("upperBody",
            CubeListBuilder.create().mirror().texOffs(45, 18).addBox(0F, 0F, 0F, 6, 4, 3),
            PartPose.offsetAndRotation(-3F, 12.46667F, -4.666667F, 0.5379539F, 0F, 0F));

        root.addOrReplaceChild("neck",
            CubeListBuilder.create().mirror().texOffs(57, 14).addBox(0F, 0F, 0F, 2, 1, 1),
            PartPose.offsetAndRotation(-1F, 11.33333F, -4F, 0.669215F, 0F, 0F));

        root.addOrReplaceChild("lowerBody",
            CubeListBuilder.create().mirror().texOffs(53, 23).addBox(0F, 0F, 0F, 2, 5, 3),
            PartPose.offset(-1F, 19F, -2.4F));

        // Spike-like fragments around the lower body (legacy "Shape*" parts).
        root.addOrReplaceChild("shape_1",
            CubeListBuilder.create().mirror().texOffs(54, 27).addBox(0F, 0F, 0F, 1, 0, 3),
            PartPose.offsetAndRotation(0F, 23F, 0F, -0.2602503F, 0F, 0F));
        root.addOrReplaceChild("shape_2",
            CubeListBuilder.create().mirror().texOffs(54, 28).addBox(0F, 0F, 0F, 1, 0, 2),
            PartPose.offsetAndRotation(-1F, 23F, 0F, -0.4833219F, 0F, 0F));
        root.addOrReplaceChild("shape_7",
            CubeListBuilder.create().mirror().texOffs(54, 30).addBox(0F, 0F, 0F, 1, 0, 1),
            PartPose.offsetAndRotation(0F, 22F, 0.2666667F, -0.9294653F, 0F, 0F));
        root.addOrReplaceChild("shape_8",
            CubeListBuilder.create().mirror().texOffs(54, 27).addBox(0F, 0F, 0F, 1, 0, 3),
            PartPose.offsetAndRotation(1F, 23F, -1.6F, 0F, 0.2230717F, 0.8179294F));
        root.addOrReplaceChild("shape_9",
            CubeListBuilder.create().mirror().texOffs(54, 28).addBox(0F, 0F, 0F, 1, 0, 2),
            PartPose.offsetAndRotation(-1F, 23F, -1.866667F, 0F, -0.2230717F, 2.249306F));
        root.addOrReplaceChild("shape_10",
            CubeListBuilder.create().mirror().texOffs(55, 27).addBox(0F, 0F, 0F, 1, 1, 0),
            PartPose.offsetAndRotation(-1F, 21F, -0.5333334F, 0F, -2.286485F, 0F));
        root.addOrReplaceChild("shape_11",
            CubeListBuilder.create().mirror().texOffs(54, 28).addBox(0F, 0F, 0F, 1, 1, 0),
            PartPose.offsetAndRotation(1F, 20F, -2F, 0F, -0.9294653F, 0F));
        root.addOrReplaceChild("shape_12",
            CubeListBuilder.create().mirror().texOffs(58, 27).addBox(0F, 0F, 0F, 0, 1, 2),
            PartPose.offsetAndRotation(-1F, 23F, -1F, 0F, -0.1115358F, 0.1858931F));
        root.addOrReplaceChild("shape_13",
            CubeListBuilder.create().mirror().texOffs(58, 27).addBox(0F, 0F, 0F, 0, 1, 2),
            PartPose.offsetAndRotation(1F, 23F, 0F, 0F, 0.1858931F, -0.3717861F));
        root.addOrReplaceChild("shape_14",
            CubeListBuilder.create().mirror().texOffs(56, 28).addBox(0F, 0F, 0F, 0, 1, 1),
            PartPose.offsetAndRotation(1F, 23F, -2F, 0F, 0.9294653F, 0F));
        root.addOrReplaceChild("shape_15",
            CubeListBuilder.create().mirror().texOffs(59, 28).addBox(0F, 0F, 0F, 0, 1, 1),
            PartPose.offsetAndRotation(-1F, 23F, -2F, 0F, -0.8365188F, 0F));

        // Headset (head + jaw + teeth) -- a composite part that rotates with
        // the entity head.  In the legacy model these were named addBox()
        // calls with explicit texture offsets via setTextureOffset; we
        // reproduce them here as separate cubes on a single ModelPart.
        PartDefinition headset = root.addOrReplaceChild("headset",
            CubeListBuilder.create()
                .texOffs(17, 0).mirror().addBox(-2F, -9F, -4F, 4, 5, 5)
                .texOffs(52, 19).addBox(1F, -4F, 1F, 1, 2, 0)
                .texOffs(52, 19).addBox(-2F, -4F, 1F, 1, 2, 0)
                .texOffs(52, 19).addBox(2F, -4F, 0F, 0, 2, 1)
                .texOffs(52, 19).addBox(-2F, -4F, 0F, 0, 2, 1)
                .texOffs(18, 13).addBox(-2F, -2F, -3F, 4, 1, 4)
                .texOffs(0, 18).addBox(-1.933333F, -2.466667F, -2F, 0, 1, 1)
                .texOffs(0, 18).addBox(1.866667F, -2.466667F, -2F, 0, 1, 1)
                .texOffs(0, 18).addBox(0.4666667F, -2.466667F, -2.933333F, 1, 1, 0)
                .texOffs(0, 18).addBox(-1.466667F, -2.466667F, -2.933333F, 1, 1, 0)
                .texOffs(0, 18).addBox(0.7333333F, -4.8F, -3.533333F, 1, 1, 0)
                .texOffs(0, 18).addBox(-1.666667F, -4.8F, -3.466667F, 1, 1, 0),
            PartPose.offset(0F, 12.26667F, -4.066667F));

        root.addOrReplaceChild("leftarmset",
            CubeListBuilder.create().mirror()
                .texOffs(40, 16).addBox(3F, 10F, -4F, 1, 9, 1)
                .texOffs(0, 0).addBox(4F, 19F, -4F, 0, 2, 1)
                .texOffs(0, 0).addBox(3F, 19F, -4F, 0, 2, 1),
            PartPose.offset(0F, 2F, 1F));

        root.addOrReplaceChild("rightarmset",
            CubeListBuilder.create().mirror()
                .texOffs(40, 16).addBox(-4F, 10F, -3F, 1, 9, 1)
                .texOffs(0, 0).addBox(-3F, 19F, -3F, 0, 2, 1)
                .texOffs(0, 0).addBox(-4F, 19F, -3F, 0, 2, 1),
            PartPose.offset(0F, 2F, 0F));

        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(EntityNightCrawler entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        // Head follows aim.
        headset.yRot = netHeadYaw  * Mth.DEG_TO_RAD;
        headset.xRot = headPitch   * Mth.DEG_TO_RAD;

        // Stiff arm swing -- the legacy mod did not animate this, but a
        // little movement reads as "alive" rather than frozen.
        float swing = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        leftArm.xRot   = -swing;
        rightArm.xRot  =  swing;
    }
}
