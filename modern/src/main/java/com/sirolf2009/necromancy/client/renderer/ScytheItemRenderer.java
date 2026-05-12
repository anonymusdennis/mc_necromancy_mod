package com.sirolf2009.necromancy.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.sirolf2009.necromancy.Reference;
import com.sirolf2009.necromancy.client.model.ScytheModel;
import com.sirolf2009.necromancy.item.NecromancyItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * 1:1 modern port of the legacy {@code ItemScytheRenderer} +
 * {@code ItemScytheBoneRenderer}.
 *
 * <p>The original mod did all of its scythe rendering as a 3D
 * {@code ModelRenderer}.  In 1.21.1 we expose the same geometry as a
 * {@link BlockEntityWithoutLevelRenderer} hooked into the item via
 * {@link net.neoforged.neoforge.client.extensions.common.IClientItemExtensions}.
 *
 * <p>The translation/rotation values come from the original
 * {@code renderScythe(...)} call sites and were tuned by trial-and-error to
 * look "right" in each {@link ItemDisplayContext}.  The legacy mod did this
 * with raw {@code GL11} matrix calls; we use {@link PoseStack} instead.
 */
public class ScytheItemRenderer extends BlockEntityWithoutLevelRenderer {

    public static final ResourceLocation TEX_BLOOD = Reference.TEXTURE_MODEL_SCYTHE;
    public static final ResourceLocation TEX_BONE  = Reference.TEXTURE_MODEL_SCYTHE_BONE;

    private ModelPart bloodRoot;
    private ModelPart boneRoot;

    public ScytheItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
    }

    /**
     * Vanilla calls {@link #onResourceManagerReload} after layer registration
     * has finished, so we (re)bake the layers here.  This keeps the BEWLR
     * usable even after {@code F3 + T} resource reloads.
     */
    @Override
    public void onResourceManagerReload(net.minecraft.server.packs.resources.ResourceManager mgr) {
        EntityModelSet set = Minecraft.getInstance().getEntityModels();
        bloodRoot = set.bakeLayer(ScytheModel.LAYER_BLOOD);
        boneRoot  = set.bakeLayer(ScytheModel.LAYER_BONE);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext ctx, PoseStack pose,
                             MultiBufferSource buffers, int light, int overlay) {

        boolean bone = stack.is(NecromancyItems.SCYTHE_BONE.get());
        ModelPart root = bone ? boneRoot : bloodRoot;
        if (root == null) return;
        ResourceLocation tex = bone ? TEX_BONE : TEX_BLOOD;

        pose.pushPose();
        applyContextTransform(ctx, pose);
        // Convert from "minecraft inches" (1/16) used by ModelRenderer to the
        // 1.21.1 PoseStack unit, plus an extra Y flip because PoseStack runs
        // upside down compared to legacy GL11 matrices.
        pose.scale(1F, -1F, -1F);
        pose.translate(0F, -1.5F, 0F);
        ScytheModel.renderRoot(root, pose, buffers.getBuffer(ScytheModel.renderType(tex)), light, overlay);
        pose.popPose();
    }

    /**
     * Mimics the per-{@code ItemRenderType} translate/rotate values the
     * legacy {@code renderScythe} method picked.  Maps modern
     * {@link ItemDisplayContext} cases to the closest old equivalents.
     */
    private static void applyContextTransform(ItemDisplayContext ctx, PoseStack pose) {
        switch (ctx) {
            case GUI -> {
                // Legacy INVENTORY: pos(0, 0.4, 0) rot(150, 60, 0) scale 0.8
                pose.translate(0.5F, 0.5F, 0.5F);
                pose.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(180F));
                pose.mulPose(com.mojang.math.Axis.XP.rotationDegrees(150F));
                pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(60F));
                pose.scale(0.6F, 0.6F, 0.6F);
            }
            case GROUND, FIXED -> {
                // Dropped item / item frame.
                pose.translate(0.5F, 0.5F, 0.5F);
                pose.scale(0.5F, 0.5F, 0.5F);
            }
            case THIRD_PERSON_RIGHT_HAND, THIRD_PERSON_LEFT_HAND -> {
                pose.translate(0.5F, 0.5F, 0.5F);
                pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-140F));
                pose.scale(0.6F, 0.6F, 0.6F);
            }
            case FIRST_PERSON_RIGHT_HAND, FIRST_PERSON_LEFT_HAND -> {
                pose.translate(0.5F, 1F, 0.5F);
                pose.mulPose(com.mojang.math.Axis.XP.rotationDegrees(-10F));
                pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(140F));
                pose.scale(0.55F, 0.55F, 0.55F);
            }
            default -> {
                pose.translate(0.5F, 0.5F, 0.5F);
            }
        }
    }
}
