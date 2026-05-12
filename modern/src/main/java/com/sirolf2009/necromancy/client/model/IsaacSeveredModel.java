package com.sirolf2009.necromancy.client.model;

import com.sirolf2009.necromancy.Reference;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.LivingEntity;

/**
 * 1:1 modern port of the legacy {@code ModelIsaacSevered} -- a biped without
 * a normal head.  In place of the head a small 2x1x2 stub is rendered to mark
 * the severed neck.  Texture is 64x32.
 *
 * <p>Used by {@code EntityIsaacBody}.
 */
public class IsaacSeveredModel<T extends LivingEntity> extends HumanoidModel<T> {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Reference.rl("isaac_severed"), "main");

    public IsaacSeveredModel(ModelPart root) {
        super(root);
        // Hide the head so vanilla animation does not move it; the neck stub
        // takes its place.  We don't fully detach it because vanilla
        // PlayerRenderer code still references the field.
        this.head.visible = false;
        this.hat.visible  = false;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0F);
        PartDefinition root = mesh.getRoot();

        // Tiny neck stub mounted where the head used to sit.
        root.addOrReplaceChild("head",
            CubeListBuilder.create().mirror().texOffs(0, 0).addBox(0F, 0F, 0F, 2, 1, 2),
            PartPose.offset(-1F, 1F, -1F));

        return LayerDefinition.create(mesh, 64, 32);
    }
}
