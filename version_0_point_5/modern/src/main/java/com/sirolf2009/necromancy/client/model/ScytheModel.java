package com.sirolf2009.necromancy.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.sirolf2009.necromancy.Reference;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;

/**
 * 1:1 modern port of the legacy {@code ModelScythe} / {@code ModelScytheBone}
 * (7 cuboids each, 64x32 UV).
 *
 * <p>The legacy mod defined two near-identical scythes that differ only in the
 * shape of their joint and blade, so we share the handle pieces and switch
 * shapes via two top-level layer definitions.
 *
 * <p>The result is rendered from {@code ScytheItemRenderer} (a
 * {@link net.minecraft.client.renderer.blockentity.BlockEntityWithoutLevelRenderer})
 * using the corresponding entity-style texture under
 * {@code textures/models/scythe.png} or {@code .../scythebone.png}.
 */
public final class ScytheModel {

    public static final ModelLayerLocation LAYER_BLOOD = new ModelLayerLocation(Reference.rl("scythe"), "main");
    public static final ModelLayerLocation LAYER_BONE  = new ModelLayerLocation(Reference.rl("scythe_bone"), "main");

    private ScytheModel() {}

    /** Standard (blood) scythe: 7 cuboids matching {@code ModelScythe}. */
    public static LayerDefinition createBloodLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("handle_middle",
            CubeListBuilder.create().mirror().texOffs(0, 0).addBox(0F, 0F, 0F, 1, 11, 1),
            PartPose.offsetAndRotation(0F, 1.7F, 0F, -0.2602503F, 0F, 0F));

        root.addOrReplaceChild("blade_edge",
            CubeListBuilder.create().mirror().texOffs(4, 0).addBox(-0.5F, -0.5F, 0F, 1, 1, 10),
            PartPose.offsetAndRotation(0.5F, -7F, 2F, 0F, 0F, 0.7853982F));

        root.addOrReplaceChild("blade_base_1",
            CubeListBuilder.create().mirror().texOffs(40, 0).addBox(0F, 0F, 0F, 1, 1, 11),
            PartPose.offset(0.2F, -8F, 1F));

        root.addOrReplaceChild("handle_bottom",
            CubeListBuilder.create().mirror().texOffs(0, 0).addBox(0F, 0F, 0F, 1, 12, 1),
            PartPose.offset(0F, 12F, -2.8F));

        root.addOrReplaceChild("handle_top",
            CubeListBuilder.create().mirror().texOffs(0, 0).addBox(0F, 0F, 0F, 1, 10, 1),
            PartPose.offset(0F, -8F, 0F));

        root.addOrReplaceChild("joint",
            CubeListBuilder.create().mirror().texOffs(0, 13).addBox(0F, 0F, 0F, 2, 2, 2),
            PartPose.offset(-0.5F, -8.1F, 0F));

        root.addOrReplaceChild("blade_base_2",
            CubeListBuilder.create().mirror().texOffs(40, 0).addBox(0F, 0F, 0F, 1, 1, 11),
            PartPose.offset(-0.2F, -8F, 1F));

        return LayerDefinition.create(mesh, 64, 32);
    }

    /** Bone scythe: same general shape, different joint/blade pieces. */
    public static LayerDefinition createBoneLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("handle_middle",
            CubeListBuilder.create().mirror().texOffs(0, 0).addBox(0F, 0F, 0F, 1, 11, 1),
            PartPose.offsetAndRotation(0F, 1.7F, 0F, -0.2602503F, 0F, 0F));

        root.addOrReplaceChild("handle_bottom",
            CubeListBuilder.create().mirror().texOffs(0, 0).addBox(0F, 0F, 0F, 1, 12, 1),
            PartPose.offset(0F, 12F, -2.8F));

        root.addOrReplaceChild("handle_top",
            CubeListBuilder.create().mirror().texOffs(0, 0).addBox(0F, 0F, 0F, 1, 10, 1),
            PartPose.offset(0F, -8F, 0F));

        root.addOrReplaceChild("joint",
            CubeListBuilder.create().mirror().texOffs(34, 0).addBox(0F, 0F, 0F, 2, 4, 4),
            PartPose.offset(-0.5F, -8.1F, -1F));

        // Blade rotation matches the legacy in-render override
        // {@code setRotation(Blade, -.1f, .06f, .7f)}.
        root.addOrReplaceChild("blade",
            CubeListBuilder.create().mirror().texOffs(0, 15).addBox(-0.5F, -0.5F, 0F, 1, 1, 15),
            PartPose.offsetAndRotation(0.5F, -7F, 1F, -0.1F, 0.06F, 0.7F));

        root.addOrReplaceChild("blade_base_left",
            CubeListBuilder.create().mirror().texOffs(0, 15).addBox(0F, 0F, 0F, 1, 1, 15),
            PartPose.offsetAndRotation(0.2F, -8F, 1F, -0.1115358F, 0F, 0F));

        root.addOrReplaceChild("blade_base_right",
            CubeListBuilder.create().mirror().texOffs(0, 15).addBox(0F, 0F, 0F, 1, 1, 15),
            PartPose.offsetAndRotation(-0.2F, -8F, 1F, -0.1115358F, 0F, 0F));

        return LayerDefinition.create(mesh, 64, 32);
    }

    /** Helper that draws a baked root part flat onto the supplied buffer. */
    public static void renderRoot(ModelPart root, PoseStack pose,
                                  VertexConsumer buffer, int packedLight, int packedOverlay) {
        root.render(pose, buffer, packedLight, packedOverlay);
    }

    public static RenderType renderType(net.minecraft.resources.ResourceLocation tex) {
        return RenderType.entityCutoutNoCull(tex);
    }
}
