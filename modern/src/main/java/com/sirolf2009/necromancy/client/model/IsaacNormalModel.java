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
 * 1:1 modern port of the legacy {@code ModelIsaacNormal}: a biped with a
 * 10x9x8 head (vs. vanilla 8x8x8).  Texture is 64x32 like the original mod.
 *
 * <p>Backed by the {@link HumanoidModel} animation logic so swing, sneak,
 * sleep, riding, etc. all behave sensibly without re-implementing them.
 *
 * <p>Used by {@code EntityIsaacNormal} and {@code EntityIsaacBlood}.
 */
public class IsaacNormalModel<T extends LivingEntity> extends HumanoidModel<T> {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Reference.rl("isaac_normal"), "main");

    public IsaacNormalModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0F);
        PartDefinition root = mesh.getRoot();

        // Replace the standard 8x8x8 head with the larger Isaac head.  The
        // X offset of -1 mirrors the legacy {@code setRotationPoint(-1F,1F,0F)}
        // because Isaac's head is laterally offset from the biped's centre.
        root.addOrReplaceChild("head",
            CubeListBuilder.create().mirror().texOffs(0, 0).addBox(-4F, -8F, -4F, 10, 9, 8),
            PartPose.offset(-1F, 1F, 0F));

        return LayerDefinition.create(mesh, 64, 32);
    }
}
