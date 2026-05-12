package com.sirolf2009.necromancy.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.sirolf2009.necromancy.api.BodyPartLocation;
import com.sirolf2009.necromancy.bodypart.BodyPartConfigManager;
import com.sirolf2009.necromancy.bodypart.BodypartFlagsJson;
import com.sirolf2009.necromancy.bodypart.MinionCompositeCollision;
import com.sirolf2009.necromancy.multipart.TransformHierarchy;
import com.sirolf2009.necromancy.multipart.collision.ResolvedObb;
import com.sirolf2009.necromancy.multipart.part.BodyPartNode;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.FastColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

/** Client debug outlines for multipart minion collision boxes (F3+B). */
public final class MinionCollisionDebugDraw {

    /** Minimum half-extent in any axis to consider an OBB non-degenerate and worth drawing. */
    private static final float MIN_OBB_HALF_EXTENT = 1e-5f;

    public static void renderMultipartBoxes(PoseStack poseStack, MultiBufferSource buffers, Vec3 camera,
                                             List<MinionCompositeCollision.SlotBox> boxes) {
        if (boxes.isEmpty()) return;
        poseStack.pushPose();
        poseStack.translate(-camera.x, -camera.y, -camera.z);
        VertexConsumer lines = buffers.getBuffer(RenderType.lines());
        PoseStack.Pose pose = poseStack.last();
        for (MinionCompositeCollision.SlotBox sb : boxes) {
            outlineBoxWorld(pose, lines, sb.worldBounds(), colorFor(sb.slot()));
        }
        poseStack.popPose();
    }

    private static int colorFor(BodyPartLocation slot) {
        return switch (slot) {
            case Head -> FastColor.ARGB32.color(255, 255, 90, 90);
            case Torso -> FastColor.ARGB32.color(255, 90, 255, 90);
            case ArmLeft -> FastColor.ARGB32.color(255, 90, 220, 255);
            case ArmRight -> FastColor.ARGB32.color(255, 255, 90, 255);
            case Legs -> FastColor.ARGB32.color(255, 255, 220, 90);
        };
    }

    // -------------------------------------------------------------------------
    // Hierarchy OBB debug draw (new multipart system)
    // -------------------------------------------------------------------------

    /**
     * Draw oriented bounding boxes for every collision-enabled node in the hierarchy.
     * Colors are assigned by bodypart flag: head=red, torso=green, arm=cyan, leg=yellow, special=purple.
     *
     * @param poseStack pose stack at entity-relative origin
     * @param buffers   buffer source
     * @param camera    absolute camera position (used to go to world-origin render space)
     * @param hierarchy the entity's transform hierarchy
     */
    public static void renderHierarchyOBBs(PoseStack poseStack,
                                            MultiBufferSource buffers,
                                            Vec3 camera,
                                            TransformHierarchy hierarchy) {
        if (hierarchy.nodes().isEmpty()) return;
        poseStack.pushPose();
        poseStack.translate(-camera.x, -camera.y, -camera.z);
        VertexConsumer lines = buffers.getBuffer(RenderType.lines());
        PoseStack.Pose pose = poseStack.last();
        for (BodyPartNode node : hierarchy.nodes()) {
            if (!node.hitbox().collisionEnabled() || !node.attachedToParent()) continue;
            ResolvedObb obb = node.simulationCollisionObb();
            // Skip degenerate (zero-volume) OBBs.
            Vector3f half = new Vector3f();
            obb.halfExtentsInto(half);
            if (half.x < MIN_OBB_HALF_EXTENT || half.y < MIN_OBB_HALF_EXTENT || half.z < MIN_OBB_HALF_EXTENT) continue;

            int color = BodyPartConfigManager.INSTANCE.get(node.id())
                .map(def -> obbColorForFlags(def.flags()))
                .orElse(FastColor.ARGB32.color(255, 200, 200, 200));

            drawObb(pose, lines, obb, color);
        }
        poseStack.popPose();
    }

    private static int obbColorForFlags(BodypartFlagsJson flags) {
        if (flags == null) return FastColor.ARGB32.color(255, 200, 200, 200);
        if (flags.head)    return FastColor.ARGB32.color(255, 0xFF, 0x5A, 0x5A); // red
        if (flags.torso)   return FastColor.ARGB32.color(255, 0x5A, 0xFF, 0x5A); // green
        if (flags.arm)     return FastColor.ARGB32.color(255, 0x5A, 0xDC, 0xFF); // cyan
        if (flags.leg)     return FastColor.ARGB32.color(255, 0xFF, 0xDC, 0x5A); // yellow
        if (flags.special) return FastColor.ARGB32.color(255, 0xDC, 0x5A, 0xFF); // purple
        return FastColor.ARGB32.color(255, 200, 200, 200);
    }

    private static void drawObb(PoseStack.Pose pose, VertexConsumer lines, ResolvedObb obb, int argb) {
        Quaternionf q = new Quaternionf();
        obb.orientationInto(q);
        Vector3f half = new Vector3f();
        obb.halfExtentsInto(half);
        Vec3 c = obb.centerWorld();

        // Compute 8 corners: ±hx, ±hy, ±hz rotated by orientation quaternion, then offset by center.
        double[][] corners = new double[8][3];
        int idx = 0;
        for (float sx : new float[]{-1f, 1f}) {
            for (float sy : new float[]{-1f, 1f}) {
                for (float sz : new float[]{-1f, 1f}) {
                    Vector3f local = new Vector3f(half.x * sx, half.y * sy, half.z * sz);
                    q.transform(local);
                    corners[idx][0] = c.x + local.x;
                    corners[idx][1] = c.y + local.y;
                    corners[idx][2] = c.z + local.z;
                    idx++;
                }
            }
        }

        // 12 edges of a box: 4 along each axis pair.
        int[][] edges = {
            {0,1},{2,3},{4,5},{6,7},   // Z-axis edges
            {0,2},{1,3},{4,6},{5,7},   // Y-axis edges
            {0,4},{1,5},{2,6},{3,7}    // X-axis edges
        };
        for (int[] e : edges) {
            segment(pose, lines,
                corners[e[0]][0], corners[e[0]][1], corners[e[0]][2],
                corners[e[1]][0], corners[e[1]][1], corners[e[1]][2],
                argb);
        }
    }

    private static void outlineBoxWorld(PoseStack.Pose pose, VertexConsumer consumer, AABB box, int argb) {
        double minX = box.minX;
        double minY = box.minY;
        double minZ = box.minZ;
        double maxX = box.maxX;
        double maxY = box.maxY;
        double maxZ = box.maxZ;
        segment(pose, consumer, minX, minY, minZ, maxX, minY, minZ, argb);
        segment(pose, consumer, minX, maxY, minZ, maxX, maxY, minZ, argb);
        segment(pose, consumer, minX, minY, maxZ, maxX, minY, maxZ, argb);
        segment(pose, consumer, minX, maxY, maxZ, maxX, maxY, maxZ, argb);
        segment(pose, consumer, minX, minY, minZ, minX, maxY, minZ, argb);
        segment(pose, consumer, maxX, minY, minZ, maxX, maxY, minZ, argb);
        segment(pose, consumer, minX, minY, maxZ, minX, maxY, maxZ, argb);
        segment(pose, consumer, maxX, minY, maxZ, maxX, maxY, maxZ, argb);
        segment(pose, consumer, minX, minY, minZ, minX, minY, maxZ, argb);
        segment(pose, consumer, maxX, minY, minZ, maxX, minY, maxZ, argb);
        segment(pose, consumer, minX, maxY, minZ, minX, maxY, maxZ, argb);
        segment(pose, consumer, maxX, maxY, minZ, maxX, maxY, maxZ, argb);
    }

    private static void segment(PoseStack.Pose pose, VertexConsumer consumer,
                                double x1, double y1, double z1, double x2, double y2, double z2, int argb) {
        float nx = (float) (x2 - x1);
        float ny = (float) (y2 - y1);
        float nz = (float) (z2 - z1);
        float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (len < 1e-5f) return;
        nx /= len;
        ny /= len;
        nz /= len;
        int r = FastColor.ARGB32.red(argb);
        int g = FastColor.ARGB32.green(argb);
        int b = FastColor.ARGB32.blue(argb);
        int a = FastColor.ARGB32.alpha(argb);
        consumer.addVertex(pose, (float) x1, (float) y1, (float) z1).setColor(r, g, b, a).setNormal(pose, nx, ny, nz);
        consumer.addVertex(pose, (float) x2, (float) y2, (float) z2).setColor(r, g, b, a).setNormal(pose, nx, ny, nz);
    }
}
