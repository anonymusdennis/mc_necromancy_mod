package com.sirolf2009.necromancy.client;

import com.sirolf2009.necromancy.api.BodyPartLocation;
import com.sirolf2009.necromancy.api.NecroEntityBase;
import com.sirolf2009.necromancy.bodypart.BodypartDefinitionJson;
import com.sirolf2009.necromancy.bodypart.BodypartHitboxJson;
import com.sirolf2009.necromancy.bodypart.BodypartVisualOffsetJson;
import com.sirolf2009.necromancy.client.model.MinionPartCache;
import com.sirolf2009.necromancy.client.renderer.BodypartPreviewRenderer;
import net.minecraft.client.model.geom.ModelPart;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * "Suggest Model And Hitbox Positioning And Scale" (SMAHPAS).
 *
 * <p>Computes a hitbox suggestion by traversing the baked {@link ModelPart} tree for the
 * current adapter and slot, collecting each cube's min/max bounds in pixel space, then
 * converting them into entity-feet-relative world coordinates using the same transform
 * that {@code MinionHierarchyRenderer} / {@link BodypartPreviewRenderer} applies at render time.
 *
 * <p>The transform for the rest pose (yaw = 0) is:
 * <pre>
 *   world_x =  modelPixels_x / 16
 *   world_y =  slotOffsetY − modelPixels_y / 16   (Y axis is flipped by scale(−1,−1,1))
 *   world_z = −modelPixels_z / 16                 (Z axis is negated by the 180° rotation)
 * </pre>
 *
 * <p>Reflection is used to read the private {@code cubes} and {@code children} fields of
 * {@link ModelPart}. NeoForge 1.21.1 ships with official Mojang field names at runtime so
 * the reflected names are stable across dev and production builds.
 */
public final class BodypartSmahpas {

    private static final Field CUBES_FIELD;
    private static final Field CHILDREN_FIELD;

    static {
        Field cubes = null;
        Field children = null;
        try {
            cubes = ModelPart.class.getDeclaredField("cubes");
            cubes.setAccessible(true);
        } catch (NoSuchFieldException ignored) {}
        try {
            children = ModelPart.class.getDeclaredField("children");
            children.setAccessible(true);
        } catch (NoSuchFieldException ignored) {}
        CUBES_FIELD    = cubes;
        CHILDREN_FIELD = children;
    }

    private BodypartSmahpas() {}

    /**
     * Computes and writes suggested hitbox values into {@code draft.hitbox}.
     * Resets the visual offset to zeroes so the mesh sits at its natural slot position.
     *
     * @param adapter the adapter for the bodypart being configured
     * @param slot    the slot the bodypart occupies
     * @param draft   the draft to update in-place
     * @return {@code true} if at least one cube was found and bounds were written; {@code false} if
     *         the adapter has no geometry for this slot (draft is left unchanged)
     */
    public static boolean suggest(NecroEntityBase adapter, BodyPartLocation slot, BodypartDefinitionJson draft) {
        adapter.updateParts();
        MinionPartCache.Baked baked = MinionPartCache.get(adapter, slot, true);
        if (baked == null) return false;

        float[] mins = { Float.MAX_VALUE,  Float.MAX_VALUE,  Float.MAX_VALUE };
        float[] maxs = {-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE };

        collectBounds(baked.root(), 0f, 0f, 0f, mins, maxs);

        if (mins[0] == Float.MAX_VALUE) return false; // no cubes found

        // Convert from model-pixel space to entity-feet-relative world space.
        // The render stack applies: translate(0, slotOffsetY, 0), scale(-1,-1,1), rotate(180°).
        // Net effect on a pixel-space vertex (vx, vy, vz) → world offset from entity feet:
        //   wx =  vx / 16       (double X-flip: scale × 180° cancel each other)
        //   wy =  slotOY − vy/16   (Y flipped by scale)
        //   wz = −vz / 16       (Z negated by 180° rotation)
        float slotOY = BodypartPreviewRenderer.slotOffsetY(slot);

        float worldMinX =  mins[0] / 16f;
        float worldMaxX =  maxs[0] / 16f;
        float worldMinY =  slotOY - maxs[1] / 16f;   // vy is flipped → min from max
        float worldMaxY =  slotOY - mins[1] / 16f;
        float worldMinZ = -maxs[2] / 16f;             // vz negated → min from max
        float worldMaxZ = -mins[2] / 16f;

        if (draft.hitbox == null) draft.hitbox = new BodypartHitboxJson();
        draft.hitbox.ox = (worldMinX + worldMaxX) / 2.0;
        draft.hitbox.oy = (worldMinY + worldMaxY) / 2.0;
        draft.hitbox.oz = (worldMinZ + worldMaxZ) / 2.0;
        draft.hitbox.sx = Math.max(0.05, worldMaxX - worldMinX);
        draft.hitbox.sy = Math.max(0.05, worldMaxY - worldMinY);
        draft.hitbox.sz = Math.max(0.05, worldMaxZ - worldMinZ);

        // Reset visual offset so the mesh is centred at the natural slot position.
        if (draft.visualOffset == null) draft.visualOffset = new BodypartVisualOffsetJson();
        draft.visualOffset.dx        = 0;
        draft.visualOffset.dy        = 0;
        draft.visualOffset.dz        = 0;
        draft.visualOffset.rotYawDeg   = 0;
        draft.visualOffset.rotPitchDeg = 0;
        draft.visualOffset.rotRollDeg  = 0;
        draft.visualOffset.scaleX    = 1;
        draft.visualOffset.scaleY    = 1;
        draft.visualOffset.scaleZ    = 1;
        return true;
    }

    // ------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private static void collectBounds(ModelPart part, float px, float py, float pz,
                                      float[] mins, float[] maxs) {
        float cx = px + part.x;
        float cy = py + part.y;
        float cz = pz + part.z;

        if (CUBES_FIELD != null) {
            try {
                List<ModelPart.Cube> cubes = (List<ModelPart.Cube>) CUBES_FIELD.get(part);
                for (ModelPart.Cube cube : cubes) {
                    expandBounds(mins, maxs, cx + cube.minX, cy + cube.minY, cz + cube.minZ);
                    expandBounds(mins, maxs, cx + cube.maxX, cy + cube.maxY, cz + cube.maxZ);
                }
            } catch (Exception ignored) {
                // Fall back to the pivot point only so we at least have a centre.
                expandBounds(mins, maxs, cx, cy, cz);
            }
        } else {
            expandBounds(mins, maxs, cx, cy, cz);
        }

        if (CHILDREN_FIELD != null) {
            try {
                Map<String, ModelPart> childMap = (Map<String, ModelPart>) CHILDREN_FIELD.get(part);
                for (ModelPart child : childMap.values()) {
                    collectBounds(child, cx, cy, cz, mins, maxs);
                }
            } catch (Exception ignored) {}
        }
    }

    private static void expandBounds(float[] mins, float[] maxs, float x, float y, float z) {
        if (x < mins[0]) mins[0] = x;
        if (y < mins[1]) mins[1] = y;
        if (z < mins[2]) mins[2] = z;
        if (x > maxs[0]) maxs[0] = x;
        if (y > maxs[1]) maxs[1] = y;
        if (z > maxs[2]) maxs[2] = z;
    }
}