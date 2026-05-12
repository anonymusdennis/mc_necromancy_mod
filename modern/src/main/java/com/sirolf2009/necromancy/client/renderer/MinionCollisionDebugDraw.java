package com.sirolf2009.necromancy.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.sirolf2009.necromancy.api.BodyPartLocation;
import com.sirolf2009.necromancy.bodypart.MinionCompositeCollision;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.FastColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** Client debug outlines for multipart minion collision boxes (F3+B). */
public final class MinionCollisionDebugDraw {

    private MinionCollisionDebugDraw() {}

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
