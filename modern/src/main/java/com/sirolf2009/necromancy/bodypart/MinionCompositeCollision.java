package com.sirolf2009.necromancy.bodypart;

import com.sirolf2009.necromancy.api.BodyPartLocation;
import com.sirolf2009.necromancy.entity.EntityMinion;
import com.sirolf2009.necromancy.multipart.TransformHierarchy;
import com.sirolf2009.necromancy.multipart.part.BodyPartNode;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/** Rough multipart AABBs for minions from bodypart definitions + slot offsets (altar-era topology). */
public final class MinionCompositeCollision {

    public record SlotBox(BodyPartLocation slot, AABB worldBounds) {}

    private MinionCompositeCollision() {}

    /** Server-only: physics / probes use this list. */
    public static List<AABB> buildWorldBoxes(EntityMinion minion) {
        List<AABB> out = new ArrayList<>(6);
        Level level = minion.level();
        if (level == null || level.isClientSide) {
            return out;
        }
        appendBoxes(minion, out);
        return List.copyOf(out);
    }

    /**
     * Client-side multipart outlines (F3+B debug). Same math as server collision boxes; no {@link Level#isClientSide} guard.
     */
    public static List<SlotBox> buildWorldBoxesForClientDebug(EntityMinion minion) {
        List<SlotBox> out = new ArrayList<>(6);
        if (minion == null || minion.level() == null) {
            return out;
        }
        float yawRad = (float) Math.toRadians(-minion.getYRot());
        float cos = (float) Math.cos(yawRad);
        float sin = (float) Math.sin(yawRad);
        Vec3 root = minion.position();
        for (BodyPartLocation loc : BodyPartLocation.values()) {
            AABB b = boxForSlot(minion, loc, root, cos, sin);
            if (b != null) out.add(new SlotBox(loc, b));
        }
        return List.copyOf(out);
    }

    private static void appendBoxes(EntityMinion minion, List<AABB> out) {
        float yawRad = (float) Math.toRadians(-minion.getYRot());
        float cos = (float) Math.cos(yawRad);
        float sin = (float) Math.sin(yawRad);
        Vec3 root = minion.position();
        for (BodyPartLocation loc : BodyPartLocation.values()) {
            AABB b = boxForSlot(minion, loc, root, cos, sin);
            if (b != null) out.add(b);
        }
    }

    static AABB boxForSlot(EntityMinion minion, BodyPartLocation loc, Vec3 root, float cos, float sin) {
        String mobName = minion.getBodyPartName(loc);
        if (mobName == null || mobName.isEmpty()) return null;
        ResourceLocation id = BodyPartItemIds.inferredPartId(mobName, loc);
        BodypartDefinition def = BodyPartConfigManager.INSTANCE.get(id).orElse(null);
        if (def == null) return null;
        Vec3 slotOffset = rotateY(slotOffset(loc), cos, sin);
        return transformBox(def.localHitbox(), root, slotOffset);
    }

    private static Vec3 slotOffset(BodyPartLocation loc) {
        return switch (loc) {
            case Head -> new Vec3(0, 1.45, 0);
            case Torso -> new Vec3(0, 0.92, 0);
            case Legs -> new Vec3(0, 0.42, 0);
            case ArmLeft -> new Vec3(-0.32, 1.05, 0);
            case ArmRight -> new Vec3(0.32, 1.05, 0);
        };
    }

    private static Vec3 rotateY(Vec3 v, float cos, float sin) {
        double x = v.x * cos - v.z * sin;
        double z = v.x * sin + v.z * cos;
        return new Vec3(x, v.y, z);
    }

    private static AABB transformBox(AABB local, Vec3 root, Vec3 offset) {
        return local.move(root.x + offset.x, root.y + offset.y, root.z + offset.z);
    }

    /** LEG-flag bodypart nodes from multipart hierarchy — counts how many are grounded. Returns 0 if airborne or client-side. */
    public static int legsHierarchyTouchGround(EntityMinion minion, TransformHierarchy hierarchy) {
        Level level = minion.level();
        if (level.isClientSide) return 0;
        int count = 0;
        for (BodyPartNode n : hierarchy.nodes()) {
            BodypartDefinition def = BodyPartConfigManager.INSTANCE.get(n.id()).orElse(null);
            if (def == null || def.flags() == null || !def.flags().leg) continue;
            AABB box = n.simulationBroadphase();
            if (box.getXsize() <= 1e-6 || box.getYsize() <= 1e-6 || box.getZsize() <= 1e-6) continue;
            if (intersectsBlockCollision(level, box.move(0, -0.07, 0).inflate(-0.02, 0, -0.02))) count++;
        }
        return count;
    }

    /** Segment–AABB hit (coarse sampling along the ray). */
    public static boolean segmentHitsAny(List<AABB> boxes, Vec3 start, Vec3 end) {
        if (boxes.isEmpty()) return false;
        Vec3 dir = end.subtract(start);
        double len = dir.length();
        if (len < 1e-6) return false;
        Vec3 step = dir.scale(1.0 / len);
        int samples = (int) Math.ceil(len / 0.08) + 1;
        for (int i = 0; i <= samples; i++) {
            Vec3 p = i == samples ? end : start.add(step.scale(Math.min(len, i * 0.08)));
            for (AABB box : boxes) {
                if (box.contains(p)) return true;
            }
        }
        return false;
    }

    /** LEG-flag bodypart box lowered slightly intersects block collision. Returns 1 if grounded, 0 otherwise. */
    public static int legsConfiguredTouchGround(EntityMinion minion) {
        Level level = minion.level();
        if (level.isClientSide) return 0;
        String mobName = minion.getBodyPartName(BodyPartLocation.Legs);
        if (mobName.isEmpty()) return 0;
        ResourceLocation id = BodyPartItemIds.inferredPartId(mobName, BodyPartLocation.Legs);
        BodypartDefinition def = BodyPartConfigManager.INSTANCE.get(id).orElse(null);
        if (def == null || def.flags() == null || !def.flags().leg) return 0;

        float yawRad = (float) Math.toRadians(-minion.getYRot());
        float cos = (float) Math.cos(yawRad);
        float sin = (float) Math.sin(yawRad);
        Vec3 root = minion.position();
        AABB box = boxForSlot(minion, BodyPartLocation.Legs, root, cos, sin);
        if (box == null) return 0;
        return intersectsBlockCollision(level, box.move(0, -0.07, 0).inflate(-0.02, 0, -0.02)) ? 1 : 0;
    }

    private static boolean intersectsBlockCollision(Level level, AABB box) {
        BlockPos min = BlockPos.containing(box.minX, box.minY, box.minZ);
        BlockPos max = BlockPos.containing(box.maxX, box.maxY, box.maxZ);
        BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int y = min.getY(); y <= max.getY(); y++) {
                for (int z = min.getZ(); z <= max.getZ(); z++) {
                    mut.set(x, y, z);
                    if (!level.getBlockState(mut).getCollisionShape(level, mut).isEmpty()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
