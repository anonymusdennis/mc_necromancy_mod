package com.sirolf2009.necromancy.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.sirolf2009.necromancy.block.entity.BlockEntityAltar;
import com.sirolf2009.necromancy.client.renderer.MinionAssembler;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

/**
 * Renders the floating preview minion above the {@code BlockAltar}.
 *
 * <p>Pulls the configured body parts from the {@link BlockEntityAltar} and
 * renders them through the shared {@link MinionAssembler} pipeline -- the
 * same pipeline used for the live {@code EntityMinion}.  This guarantees the
 * preview matches what the player will summon, including correct quadruped
 * torso flips, multi-leg layouts, tentacle anims, etc.
 */
public class AltarBlockEntityRenderer implements BlockEntityRenderer<BlockEntityAltar> {

    public AltarBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public void render(BlockEntityAltar altar, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {

        var preview = altar.getPreviewParts();
        if (preview.isEmpty()) return;

        pose.pushPose();
        // Centre over the altar slab and lift to ~1.5 blocks above.
        pose.translate(0.5F, 1.5F, 0.5F);
        // Slow vertical bob so the preview looks alive.
        double bob = 0.05 * Math.sin(0.001 * System.currentTimeMillis());
        pose.translate(0F, (float) bob, 0F);
        // Slow Y rotation so all sides are visible.
        float spin = (System.currentTimeMillis() % 9000L) / 9000F * 360F;
        pose.mulPose(Axis.YP.rotationDegrees(spin));
        // Half-size and flipped to model space (same convention as RenderMinion).
        pose.scale(-0.5F, -0.5F, 0.5F);

        // No live entity -> pass null for the LivingEntity arg, no walk/attack.
        MinionAssembler.renderAssembled(null, preview, false,
            0F,                       // attackAnim
            0F, 0F,                   // walk, speed
            (float) (System.currentTimeMillis() % 1_000_000L) / 50F, // age (for any time-based anim)
            0F, 0F,                   // headYaw, headPitch
            pose, buffers, packedLight);

        pose.popPose();
    }

    @Override public boolean shouldRenderOffScreen(BlockEntityAltar be) { return false; }
}
