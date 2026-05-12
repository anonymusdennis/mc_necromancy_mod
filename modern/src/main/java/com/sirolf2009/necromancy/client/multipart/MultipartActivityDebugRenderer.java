package com.sirolf2009.necromancy.client.multipart;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.sirolf2009.necromancy.multipart.RootMobEntity;
import com.sirolf2009.necromancy.multipart.broadphase.MultipartPartActivityState;
import com.sirolf2009.necromancy.multipart.part.BodyPartNode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

/**
 * Draws tiny RGB crosses at bodypart origins coloured by {@link MultipartPartActivityState} when F3 debug is visible.
 */
public final class MultipartActivityDebugRenderer {

    private static final float MAX_DISTANCE = 48f;
    private static final int MAX_MOBS = 6;

    private MultipartActivityDebugRenderer() {}

    @SubscribeEvent
    public static void afterEntities(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;

        Minecraft mc = Minecraft.getInstance();
        if (!mc.getDebugOverlay().showDebugScreen() || mc.player == null || mc.level == null) return;

        Level level = mc.level;
        Vec3 camPos = event.getCamera().getPosition();

        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        VertexConsumer lines = buffers.getBuffer(RenderType.lines());
        PoseStack pose = event.getPoseStack();

        pose.pushPose();
        pose.translate(-camPos.x, -camPos.y, -camPos.z);
        PoseStack.Pose poseLast = pose.last();

        AABB query = mc.player.getBoundingBox().inflate(MAX_DISTANCE);
        int drawn = 0;
        for (LivingEntity le : level.getEntitiesOfClass(LivingEntity.class, query)) {
            if (!(le instanceof RootMobEntity root)) continue;
            if (le.distanceToSqr(camPos.x, camPos.y, camPos.z) > MAX_DISTANCE * MAX_DISTANCE) continue;
            if (drawn >= MAX_MOBS) break;

            for (BodyPartNode node : root.multipartHierarchy().nodes()) {
                Vec3 p = node.simulationWorldPose().position();
                int argb = argbFor(node.partActivityState());
                float half = 0.035f;
                vertexLine(poseLast, lines, p.add(-half, 0, 0), p.add(half, 0, 0), argb);
                vertexLine(poseLast, lines, p.add(0, -half, 0), p.add(0, half, 0), argb);
                vertexLine(poseLast, lines, p.add(0, 0, -half), p.add(0, 0, half), argb);
            }
            drawn++;
        }

        pose.popPose();
        buffers.endBatch(RenderType.lines());
    }

    private static int argbFor(MultipartPartActivityState s) {
        return switch (s) {
            case ACTIVE -> FastColor.ARGB32.color(210, 80, 220, 90);
            case IDLE -> FastColor.ARGB32.color(190, 230, 210, 160);
            case SLEEPING -> FastColor.ARGB32.color(200, 230, 120, 110);
            case STATIC -> FastColor.ARGB32.color(210, 140, 210, 235);
        };
    }

    private static void vertexLine(PoseStack.Pose pose, VertexConsumer consumer, Vec3 a, Vec3 b, int argb) {
        float nx = (float) (b.x - a.x);
        float ny = (float) (b.y - a.y);
        float nz = (float) (b.z - a.z);
        float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (len < 1.0e-5f) return;
        nx /= len;
        ny /= len;
        nz /= len;

        int r = FastColor.ARGB32.red(argb);
        int g = FastColor.ARGB32.green(argb);
        int bl = FastColor.ARGB32.blue(argb);
        int al = FastColor.ARGB32.alpha(argb);

        consumer.addVertex(pose, (float) a.x, (float) a.y, (float) a.z)
            .setColor(r, g, bl, al)
            .setNormal(pose, nx, ny, nz);
        consumer.addVertex(pose, (float) b.x, (float) b.y, (float) b.z)
            .setColor(r, g, bl, al)
            .setNormal(pose, nx, ny, nz);
    }
}
