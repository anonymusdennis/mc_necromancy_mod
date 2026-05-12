package com.sirolf2009.necromancy.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.sirolf2009.necromancy.api.BodyPartLocation;
import com.sirolf2009.necromancy.entity.EntityMinion;
import com.sirolf2009.necromancy.entity.MinionAssembly;
import com.sirolf2009.necromancy.bodypart.MinionCompositeCollision;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * Custom entity renderer for {@link EntityMinion}.
 *
 * <p>This renderer applies the standard {@link net.minecraft.client.renderer.entity.LivingEntityRenderer}
 * outer transform (lift, mirror Y, body-yaw rotate) and then delegates the
 * body-part assembly to {@link MinionAssembler}, which is shared with the
 * altar preview renderer.
 *
 * <p>When the new multipart system is active ({@code !useLegacyCollision()} and the hierarchy
 * is non-empty), rendering is delegated to {@link MinionHierarchyRenderer} which reads animation
 * overlays from the {@link com.sirolf2009.necromancy.multipart.TransformHierarchy} instead of
 * calling the legacy {@code setAnim} hook.  The legacy {@link MinionAssembler} path is preserved
 * as an automatic fallback.
 *
 * <p>F3+B hitbox drawing also switches: the new path draws per-part OBBs from the hierarchy via
 * {@link MinionCollisionDebugDraw#renderHierarchyOBBs}; the legacy path uses slot AABBs.
 */
public class RenderMinion extends EntityRenderer<EntityMinion> {

    /** Used for the entity texture lookup; the actual draw uses per-part textures. */
    private static final ResourceLocation FALLBACK =
        ResourceLocation.withDefaultNamespace("textures/entity/zombie/zombie.png");

    public RenderMinion(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.shadowRadius = 0.5F;
    }

    @Override
    public ResourceLocation getTextureLocation(EntityMinion entity) {
        var head = entity.getBodyPart(BodyPartLocation.Head);
        return head == null ? FALLBACK : head.texture;
    }

    @Override
    public void render(EntityMinion minion, float entityYaw, float partialTicks,
                       PoseStack pose, MultiBufferSource buf, int light) {
        super.render(minion, entityYaw, partialTicks, pose, buf, light);

        // Use the entity's already-resolved MinionAssembly snapshot so render
        // and movement see exactly the same body-part state.
        MinionAssembly assembly = minion.getAssembly();

        float bodyYaw   = Mth.lerp(partialTicks, minion.yBodyRotO, minion.yBodyRot);
        float headYaw   = Mth.lerp(partialTicks, minion.yHeadRotO, minion.yHeadRot) - bodyYaw;
        float headPitch = Mth.lerp(partialTicks, minion.xRotO, minion.getXRot());
        float age       = (float) minion.tickCount + partialTicks;
        float walk      = minion.walkAnimation.position(partialTicks);
        float speed     = Mth.clamp(minion.walkAnimation.speed(partialTicks), 0F, 1F);
        float attackAnim = minion.getAttackAnim(partialTicks);

        boolean useHierarchy = !minion.useLegacyCollision()
            && !minion.multipartHierarchy().nodes().isEmpty();

        if (useHierarchy) {
            // New multipart path: hierarchy-driven animation overlays.
            MinionHierarchyRenderer.render(minion, minion.multipartHierarchy(), assembly,
                bodyYaw, partialTicks, pose, buf, light);
        } else {
            // Legacy path: standard setAnim pipeline.
            pose.pushPose();
            pose.translate(0F, 1.5F, 0F);
            pose.scale(-1F, -1F, 1F);
            pose.mulPose(Axis.YP.rotationDegrees(180F - bodyYaw));

            MinionAssembler.renderAssembled(minion, assembly, minion.isSaddled(),
                attackAnim, walk, speed, age, headYaw, headPitch,
                pose, buf, light);

            pose.popPose();
        }

        if (entityRenderDispatcher.shouldRenderHitBoxes()) {
            Vec3 cam = entityRenderDispatcher.camera.getPosition();
            if (useHierarchy) {
                // New path: draw per-part OBBs from the hierarchy.
                MinionCollisionDebugDraw.renderHierarchyOBBs(pose, buf, cam,
                    minion.multipartHierarchy());
            } else {
                // Legacy path: draw slot AABBs.
                var dbg = MinionCompositeCollision.buildWorldBoxesForClientDebug(minion);
                if (!dbg.isEmpty()) {
                    MinionCollisionDebugDraw.renderMultipartBoxes(pose, buf, cam, dbg);
                }
            }
        }
    }
}
