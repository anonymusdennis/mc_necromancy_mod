package com.sirolf2009.necromancy.client.model;

import com.sirolf2009.necromancy.Reference;
import com.sirolf2009.necromancy.entity.EntityIsaacHead;
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
 * 1:1 modern port of the legacy {@code ModelIsaacHead}: a flying head with a
 * trail of four little neck cubes.  Texture is 64x32.
 *
 * <p>Used by {@code EntityIsaacHead}.
 */
public class IsaacHeadModel extends HierarchicalModel<EntityIsaacHead> {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Reference.rl("isaac_head"), "main");

    private final ModelPart root;
    private final ModelPart head;

    public IsaacHeadModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
    }

    @Override
    public ModelPart root() { return root; }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("head",
            CubeListBuilder.create().mirror().texOffs(0, 0).addBox(-4F, -8F, -4F, 10, 9, 8),
            PartPose.offset(0F, 1F, 0F));

        // Four neck cubes that hover behind the head.
        root.addOrReplaceChild("neck1",
            CubeListBuilder.create().mirror().texOffs(0, 0).addBox(0F, 0F, 0F, 1, 1, 1),
            PartPose.offset(1F, 2F, -1F));
        root.addOrReplaceChild("neck2",
            CubeListBuilder.create().mirror().texOffs(0, 0).addBox(0F, 0F, 0F, 1, 1, 1),
            PartPose.offset(0F, 2F, 0F));
        root.addOrReplaceChild("neck3",
            CubeListBuilder.create().mirror().texOffs(0, 0).addBox(0F, 0F, 0F, 1, 1, 1),
            PartPose.offset(0F, 2F, 1F));
        root.addOrReplaceChild("neck4",
            CubeListBuilder.create().mirror().texOffs(0, 0).addBox(0F, 0F, 0F, 1, 3, 1),
            PartPose.offset(1F, 2F, 1F));

        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(EntityIsaacHead entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        head.yRot = netHeadYaw * Mth.DEG_TO_RAD;
        head.xRot = headPitch  * Mth.DEG_TO_RAD;
    }
}
