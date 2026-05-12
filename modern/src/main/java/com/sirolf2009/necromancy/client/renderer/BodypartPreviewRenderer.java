package com.sirolf2009.necromancy.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.sirolf2009.necromancy.api.NecroEntityBase;
import com.sirolf2009.necromancy.api.NecroEntityRegistry;
import com.sirolf2009.necromancy.bodypart.BodypartAttachmentJson;
import com.sirolf2009.necromancy.bodypart.BodypartDevLiveDraft;
import com.sirolf2009.necromancy.bodypart.BodypartDefinition;
import com.sirolf2009.necromancy.bodypart.BodypartPreviewDiagnostics;
import com.sirolf2009.necromancy.bodypart.BodypartPreviewDraftResolution;
import com.sirolf2009.necromancy.bodypart.BodypartPreviewGeom;
import com.sirolf2009.necromancy.bodypart.BodypartPreviewMask;
import com.sirolf2009.necromancy.entity.EntityBodypartPreview;
import com.sirolf2009.necromancy.item.ItemBodyPart;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

/**
 * Bodypart dev preview: adapter mesh via {@link MinionAssembler}; optional collision wireframe and small line crosses at sockets/pivots.
 */
public class BodypartPreviewRenderer extends EntityRenderer<EntityBodypartPreview> {

    private static final float BASE_MODEL_FLIP = 0.55F;
    /** Half-extent of socket marker crosses (blocks); pivot markers use {@link #PIVOT_CROSS_HALF}. */
    private static final double SOCKET_CROSS_HALF = 0.035;
    private static final double PIVOT_CROSS_HALF = 0.024;

    public BodypartPreviewRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.shadowRadius = 0f;
    }

    @Override
    public ResourceLocation getTextureLocation(EntityBodypartPreview entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }

    @Override
    public void render(EntityBodypartPreview entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight) {
        int lm = BodypartDevLiveDraft.livePreviewMask(entity.getDevBlockPacked());
        int mask = lm != BodypartDevLiveDraft.NO_LIVE_MASK ? lm : entity.getPreviewVisibilityMask();
        boolean showModel = (mask & BodypartPreviewMask.MESH) != 0;
        boolean showCollision = (mask & BodypartPreviewMask.COLLISION_OUTLINE) != 0;
        boolean showSocketMarkers = (mask & BodypartPreviewMask.SOCKET_MARKERS) != 0;
        boolean showPivotMarkers = (mask & BodypartPreviewMask.PIVOT_MARKERS) != 0;

        Vec3 cam = entityRenderDispatcher.camera.getPosition();
        double x = entity.getX(partialTicks);
        double y = entity.getY(partialTicks);
        double z = entity.getZ(partialTicks);
        float yaw = entity.getYRot();

        Minecraft mc = Minecraft.getInstance();
        Optional<BodypartDefinition> resolved =
            mc.level != null ? BodypartPreviewDraftResolution.resolve(entity, mc.level) : Optional.empty();

        BodypartPreviewDiagnostics.logRenderSample(entity, partialTicks, y, showModel);

        if (mc.level != null && resolved.isPresent()) {
            BodypartDefinition def = resolved.get();
            if (showCollision || showSocketMarkers || showPivotMarkers) {
                poseStack.pushPose();
                poseStack.translate(-cam.x, -cam.y, -cam.z);
                VertexConsumer lines = buffers.getBuffer(RenderType.lines());
                PoseStack.Pose pose = poseStack.last();
                if (showCollision) {
                    int rgba = FastColor.ARGB32.color(255, 220, 220, 60);
                    drawOrientedHitbox(pose, lines, def.localHitbox(), x, y, z, yaw, rgba);
                }
                if (showSocketMarkers || showPivotMarkers) {
                    drawAttachmentMarkers(pose, lines, def, x, y, z, yaw, showSocketMarkers, showPivotMarkers);
                }
                poseStack.popPose();
            }
        }

        if (showModel && mc.level != null) {
            ResourceLocation pid = entity.getPartIdRl();
            if (pid != null) {
                Item stackItem = BuiltInRegistries.ITEM.getOptional(pid).orElse(Items.AIR);
                if (stackItem instanceof ItemBodyPart bp) {
                    NecroEntityBase adapter = NecroEntityRegistry.get(bp.getMobName());
                    if (adapter != null) {
                        poseStack.pushPose();
                        poseStack.translate(-cam.x, -cam.y, -cam.z);
                        poseStack.translate((float) x, (float) y, (float) z);
                        poseStack.translate(0F, 1.5F, 0F);
                        poseStack.scale(-BASE_MODEL_FLIP, -BASE_MODEL_FLIP, BASE_MODEL_FLIP);
                        poseStack.mulPose(Axis.YP.rotationDegrees(180F - yaw));
                        MinionAssembler.renderSinglePartAtRest(entity, adapter, bp.getLocation(),
                            resolved.orElse(null), poseStack, buffers, packedLight);
                        poseStack.popPose();
                    }
                }
            }
        }

        super.render(entity, entityYaw, partialTicks, poseStack, buffers, packedLight);
    }

    private static void drawAttachmentMarkers(PoseStack.Pose pose, VertexConsumer lines, BodypartDefinition def,
                                              double ex, double ey, double ez, float yawDeg,
                                              boolean sockets, boolean pivots) {
        if (def.attachments() == null || def.attachments().isEmpty()) return;
        int argbSocket = FastColor.ARGB32.color(255, 255, 140, 40);
        int argbPivot = FastColor.ARGB32.color(255, 70, 200, 255);
        for (BodypartAttachmentJson at : def.attachments()) {
            if (at == null) continue;
            if (sockets) {
                Vec3 local = new Vec3(at.ox, at.oy, at.oz);
                Vec3 off = BodypartPreviewGeom.rotateY(local, yawDeg);
                drawPointCross(pose, lines, ex + off.x, ey + off.y, ez + off.z, SOCKET_CROSS_HALF, argbSocket);
            }
            if (pivots && at.hasRotationPivot) {
                Vec3 lp = new Vec3(at.pivotOx, at.pivotOy, at.pivotOz);
                lp = BodypartPreviewGeom.rotateY(lp, yawDeg);
                drawPointCross(pose, lines, ex + lp.x, ey + lp.y, ez + lp.z, PIVOT_CROSS_HALF, argbPivot);
            }
        }
    }

    /** Three orthogonal segments through the point — same line pipeline as hitbox wireframe. */
    private static void drawPointCross(PoseStack.Pose pose, VertexConsumer lines,
                                      double x, double y, double z, double half, int argb) {
        segment(pose, lines, new double[]{x - half, y, z}, new double[]{x + half, y, z}, argb);
        segment(pose, lines, new double[]{x, y - half, z}, new double[]{x, y + half, z}, argb);
        segment(pose, lines, new double[]{x, y, z - half}, new double[]{x, y, z + half}, argb);
    }

    /** Rotate local AABB corners by yaw around entity anchor, draw 12 edges (camera-relative parent already applied). */
    private static void drawOrientedHitbox(PoseStack.Pose pose, VertexConsumer lines, AABB local,
                                           double ex, double ey, double ez, float yawDeg, int argb) {
        double[][] p = new double[8][3];
        int k = 0;
        for (double vx : new double[]{local.minX, local.maxX}) {
            for (double vy : new double[]{local.minY, local.maxY}) {
                for (double vz : new double[]{local.minZ, local.maxZ}) {
                    Vec3 r = BodypartPreviewGeom.rotateY(new Vec3(vx, vy, vz), yawDeg);
                    p[k][0] = ex + r.x;
                    p[k][1] = ey + r.y;
                    p[k][2] = ez + r.z;
                    k++;
                }
            }
        }
        int[][] edges = {
            {0, 1}, {2, 3}, {4, 5}, {6, 7},
            {0, 2}, {1, 3}, {4, 6}, {5, 7},
            {0, 4}, {1, 5}, {2, 6}, {3, 7}
        };
        for (int[] e : edges) {
            segment(pose, lines, p[e[0]], p[e[1]], argb);
        }
    }

    private static void segment(PoseStack.Pose pose, VertexConsumer consumer, double[] a, double[] b, int argb) {
        float nx = (float) (b[0] - a[0]);
        float ny = (float) (b[1] - a[1]);
        float nz = (float) (b[2] - a[2]);
        float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (len < 1e-5f) return;
        nx /= len;
        ny /= len;
        nz /= len;
        int r = FastColor.ARGB32.red(argb);
        int g = FastColor.ARGB32.green(argb);
        int bl = FastColor.ARGB32.blue(argb);
        int al = FastColor.ARGB32.alpha(argb);
        consumer.addVertex(pose, (float) a[0], (float) a[1], (float) a[2]).setColor(r, g, bl, al).setNormal(pose, nx, ny, nz);
        consumer.addVertex(pose, (float) b[0], (float) b[1], (float) b[2]).setColor(r, g, bl, al).setNormal(pose, nx, ny, nz);
    }
}
