package com.sirolf2009.necromancy.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.sirolf2009.necromancy.entity.EntityMinion;
import com.sirolf2009.necromancy.item.NecromancyItems;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * When the local player wears {@link com.sirolf2009.necromancy.item.ItemNecroGoggles},
 * draws faint lines for movement destinations and combat targets, plus a tiny health notch.
 * Deliberately low-alpha, distance-limited, and capped entity count to stay readable in combat.
 */
public final class MinionInsightRenderer {

    /** Blocks from camera — farther minions stay visually quiet. */
    private static final float MAX_CAMERA_DISTANCE = 44f;
    /** Hard cap so crowded armies do not spray lines everywhere. */
    private static final int MAX_MINIONS_DRAWN = 14;

    private MinionInsightRenderer() {}

    @SubscribeEvent
    public static void afterEntities(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        Level level = mc.level;
        if (player == null || level == null) return;
        if (!isInsightEquipped(player)) return;

        Camera camera = event.getCamera();
        Vec3 camPos = camera.getPosition();

        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        VertexConsumer lines = buffers.getBuffer(RenderType.lines());

        PoseStack pose = event.getPoseStack();
        DeltaTracker dt = event.getPartialTick();
        float pt = dt.getGameTimeDeltaPartialTick(false);

        pose.pushPose();
        pose.translate(-camPos.x, -camPos.y, -camPos.z);

        PoseStack.Pose poseLast = pose.last();

        AABB query = player.getBoundingBox().inflate(MAX_CAMERA_DISTANCE);
        List<EntityMinion> nearby = level.getEntitiesOfClass(EntityMinion.class, query);

        nearby.sort(Comparator.comparingDouble(a -> a.distanceToSqr(camPos.x, camPos.y, camPos.z)));

        int drawn = 0;
        for (EntityMinion minion : nearby) {
            if (drawn >= MAX_MINIONS_DRAWN) break;
            if (minion.distanceToSqr(camPos.x, camPos.y, camPos.z) > MAX_CAMERA_DISTANCE * MAX_CAMERA_DISTANCE) {
                continue;
            }

            Vec3 feet = minion.getPosition(pt).add(0.0, minion.getBbHeight() * 0.55, 0.0);

            Optional<BlockPos> nav = minion.getSyncedNavDestination();
            if (nav.isPresent()) {
                Vec3 dest = Vec3.atBottomCenterOf(nav.get()).add(0.0, 0.05, 0.0);
                int argb = FastColor.ARGB32.color(38, 130, 210, 235); // muted teal, low alpha
                vertexLine(poseLast, lines, feet, dest, argb);
            }

            Optional<UUID> atkId = minion.getSyncedAttackTargetId();
            if (atkId.isPresent()) {
                LivingEntity victim = findLiving(level, atkId.get(), query.inflate(8));
                if (victim != null && victim.isAlive()) {
                    Vec3 tgt = victim.getEyePosition(pt);
                    int argb = FastColor.ARGB32.color(52, 230, 120, 75); // soft amber/red mix
                    vertexLine(poseLast, lines, feet.add(0, 0.08, 0), tgt, argb);
                }
            }

            drawHealthNotch(poseLast, lines, minion, pt);
            drawn++;
        }

        pose.popPose();
        buffers.endBatch(RenderType.lines());
    }

    private static boolean isInsightEquipped(Player player) {
        return player.getInventory().getArmor(3).is(NecromancyItems.NECRO_GOGGLES.get());
    }

    private static LivingEntity findLiving(Level level, UUID id, AABB box) {
        for (LivingEntity le : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (id.equals(le.getUUID())) {
                return le;
            }
        }
        return null;
    }

    /** Thin horizontal segment above the mob — length scales with HP fraction. */
    private static void drawHealthNotch(
        PoseStack.Pose poseLast,
        VertexConsumer buf,
        LivingEntity mob,
        float pt
    ) {
        float max = mob.getMaxHealth();
        if (max <= 1.0e-4f) return;
        float ratio = Math.min(1f, mob.getHealth() / max);
        Vec3 base = mob.getPosition(pt).add(0.0, mob.getBbHeight() + 0.22, 0.0);
        float half = 0.18f;
        float span = half * 2f * ratio;
        float rf = 1f - ratio;
        int argb = FastColor.ARGB32.color(55,
            (int) (80 + 120 * rf),
            (int) (170 + 60 * ratio),
            (int) (110 + 80 * ratio));

        Vec3 left = base.add(-half, 0, 0);
        Vec3 right = base.add(-half + span, 0, 0);
        vertexLine(poseLast, buf, left, right, argb);
    }

    private static void vertexLine(
        PoseStack.Pose pose,
        VertexConsumer consumer,
        Vec3 a,
        Vec3 b,
        int argb
    ) {
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
