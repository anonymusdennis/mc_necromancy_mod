package com.sirolf2009.necromancy.client.model;

import com.sirolf2009.necromancy.api.BodyPart;
import com.sirolf2009.necromancy.api.BodyPartLocation;
import com.sirolf2009.necromancy.api.NecroEntityBase;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lazily bakes a {@link ModelPart} hierarchy for the body parts declared on a
 * {@link NecroEntityBase} adapter.  Caches per-(adapter, location) so we only
 * pay the conversion cost once per game session.
 *
 * <p>This is the bridge between the legacy declarative {@link BodyPart}
 * representation and the modern {@link ModelPart}/{@link LayerDefinition}
 * pipeline.  The {@link Baked} record additionally exposes:
 * <ul>
 *     <li>the individual child parts in the same order as the source
 *         {@code BodyPart[]}, so adapters can mutate {@code xRot/yRot/zRot}
 *         per part during animation, and</li>
 *     <li>the original {@link PartPose} of each part, so the renderer can
 *         {@link ModelPart#loadPose(PartPose) reset} them every frame and
 *         prevent animation values from compounding.</li>
 * </ul>
 */
public final class MinionPartCache {

    /**
     * Baked entry containing everything the renderer needs to draw + animate
     * a body-part group:
     * <ul>
     *     <li>{@code root} -- the parent {@link ModelPart} containing all
     *         children (one per source {@link BodyPart})</li>
     *     <li>{@code children} -- the children in declaration order; passed
     *         to {@link NecroEntityBase#setAnim} so adapters can manipulate
     *         them by index without name lookups</li>
     *     <li>{@code initialPoses} -- one entry per child holding its
     *         constructor-time {@link PartPose}, used to reset state per
     *         frame</li>
     * </ul>
     */
    public record Baked(ModelPart root, ModelPart[] children, PartPose[] initialPoses) {

        /** Reset every child to its initial pose (call once per frame, before {@code setAnim}). */
        public void resetPoses() {
            root.xRot = 0F;
            root.yRot = 0F;
            root.zRot = 0F;
            for (int i = 0; i < children.length; i++) {
                children[i].loadPose(initialPoses[i]);
            }
        }
    }

    private static final Map<String, Baked> CACHE = new ConcurrentHashMap<>();

    private MinionPartCache() {}

    /** Returns a baked, render-ready {@link Baked} entry for the given location. */
    public static Baked get(NecroEntityBase base, BodyPartLocation loc) {
        return get(base, loc, false);
    }

    /**
     * When {@code isolateForPreview} is true, use a separate cache entry from in-world minions so animated
     * {@link ModelPart} state on shared adapters cannot leak into bodypart-dev preview meshes between frames.
     */
    public static Baked get(NecroEntityBase base, BodyPartLocation loc, boolean isolateForPreview) {
        if (base == null) return null;
        base.updateParts();
        String key = base.mobName + ":" + loc + (isolateForPreview ? ":preview" : "");
        return CACHE.computeIfAbsent(key, k -> bake(base, loc));
    }

    private static Baked bake(NecroEntityBase base, BodyPartLocation loc) {
        BodyPart[] parts = switch (loc) {
            case Head     -> base.head;
            case Torso    -> base.torso;
            case ArmLeft  -> base.armLeft;
            case ArmRight -> base.armRight;
            case Legs     -> base.legs;
        };
        if (parts == null || parts.length == 0) return null;

        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        for (int i = 0; i < parts.length; i++) {
            BodyPart bp = parts[i];
            // Mirror is preserved by re-applying it on the CubeListBuilder
            // before {@code addOrReplaceChild} consumes it.  See {@link BodyPart#mirror}.
            if (bp.mirror) {
                bp.cubes.mirror(true);
            }
            root.addOrReplaceChild("p" + i, bp.cubes, bp.pose);
        }
        ModelPart baked = LayerDefinition.create(mesh, base.textureWidth, base.textureHeight)
            .bakeRoot();

        ModelPart[] children = new ModelPart[parts.length];
        PartPose[] initial   = new PartPose[parts.length];
        for (int i = 0; i < parts.length; i++) {
            children[i] = baked.getChild("p" + i);
            initial[i]  = parts[i].pose;
        }
        return new Baked(baked, children, initial);
    }
}
