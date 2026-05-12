package com.sirolf2009.necromancy.client.model;

import com.sirolf2009.necromancy.api.BodyPart;
import com.sirolf2009.necromancy.api.NecroEntityBase;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper that adapts a vanilla {@link LayerDefinition} into our
 * {@link BodyPart} representation so adapters do not have to retype every
 * cuboid by hand.
 *
 * <p>Intended for the next wave of vanilla-mob adapters (allay, camel,
 * axolotl, ...) where the mob's geometry already exists as a vanilla
 * {@code ModelLayers.X} layer definition.  The four explicit {@code Cow},
 * {@code Pig}, {@code Sheep}, {@code Squid}, ... adapters from this round
 * keep their hand-typed geometry; this helper is groundwork only.
 *
 * <h2>How it works</h2>
 * Vanilla model definitions are private records ({@link PartDefinition},
 * {@code CubeDefinition}) without accessor methods.  This class uses
 * {@code setAccessible(true)} reflection to walk a {@link MeshDefinition}
 * and pull out every cube's:
 * <ul>
 *     <li>origin (x,y,z) and dimensions (sx,sy,sz)</li>
 *     <li>UV offset (u,v)</li>
 *     <li>cube grow / mirror</li>
 * </ul>
 * It then re-issues those cubes as {@link CubeListBuilder#addBox} calls on a
 * fresh {@link BodyPart}.  This works because Minecraft / NeoForge dev runs
 * with {@code --add-opens} for the relevant modules.
 *
 * <p>If reflection fails (different Minecraft version, hardened module), an
 * empty array is returned and a warning is logged.  Adapters that rely on
 * borrowing should provide a hand-typed fallback.
 */
public final class BorrowedGeometry {

    private static final Logger LOG = LoggerFactory.getLogger("Necromancy/BorrowedGeometry");

    private BorrowedGeometry() {}

    /**
     * Convert one named part of a {@link LayerDefinition} into a single
     * {@link BodyPart} owned by {@code owner}.  All cubes of that part are
     * collapsed into one BodyPart's {@link CubeListBuilder}, preserving UV
     * offsets per cube.
     *
     * <p>Nested parts can be addressed with slash separators:
     * {@code borrow(... "body/head" ...)}.
     */
    public static BodyPart[] borrow(NecroEntityBase owner, LayerDefinition src, String partName) {
        try {
            MeshDefinition mesh = (MeshDefinition) FIELD_LAYER_MESH.get(src);
            PartDefinition root = mesh.getRoot();
            PartDefinition part = findChild(root, partName);
            if (part == null) {
                LOG.warn("Layer has no part '{}' -- returning empty BodyPart[]", partName);
                return new BodyPart[0];
            }
            BodyPart bp = bodyPartFromDefinition(owner, part);
            return new BodyPart[] { bp };
        } catch (ReflectiveOperationException e) {
            LOG.warn("Reflective extraction failed; adapter must fall back to hand-typed cubes", e);
            return new BodyPart[0];
        }
    }

    /**
     * Like {@link #borrow}, but yields one {@link BodyPart} per requested
     * part name -- handy for limbs, where each leg is its own part.
     */
    public static BodyPart[] borrowAll(NecroEntityBase owner, LayerDefinition src, List<String> partNames) {
        try {
            MeshDefinition mesh = (MeshDefinition) FIELD_LAYER_MESH.get(src);
            PartDefinition root = mesh.getRoot();
            List<BodyPart> out = new ArrayList<>();
            for (String n : partNames) {
                PartDefinition part = findChild(root, n);
                if (part == null) {
                    LOG.warn("Layer has no part '{}'", n);
                    continue;
                }
                out.add(bodyPartFromDefinition(owner, part));
            }
            return out.toArray(new BodyPart[0]);
        } catch (ReflectiveOperationException e) {
            LOG.warn("Reflective extraction failed; adapter must fall back to hand-typed cubes", e);
            return new BodyPart[0];
        }
    }

    // ------------------------------------------------------------------ --

    private static BodyPart bodyPartFromDefinition(NecroEntityBase owner, PartDefinition def)
            throws ReflectiveOperationException {

        @SuppressWarnings("unchecked")
        List<Object> cubes = (List<Object>) FIELD_PART_CUBES.get(def);
        PartPose pose      = (PartPose)     FIELD_PART_POSE.get(def);

        BodyPart bp = new BodyPart(owner, 0, 0);
        for (Object c : cubes) {
            Vector3f origin = (Vector3f) FIELD_CUBE_ORIGIN.get(c);
            Vector3f dim    = (Vector3f) FIELD_CUBE_DIM.get(c);
            CubeDeformation grow = (CubeDeformation) FIELD_CUBE_GROW.get(c);
            boolean mirror = (boolean) FIELD_CUBE_MIRROR.get(c);

            int u = readUv(c, FIELD_CUBE_TEXCOORD, FIELD_UV_U);
            int v = readUv(c, FIELD_CUBE_TEXCOORD, FIELD_UV_V);

            bp.cubes.texOffs(u, v).mirror(mirror);
            bp.cubes.addBox(origin.x(), origin.y(), origin.z(),
                            dim.x(), dim.y(), dim.z(), grow);
        }
        bp.pose   = pose;
        bp.mirror = false; // cubes have already absorbed mirror per-cube
        return bp;
    }

    private static int readUv(Object cube, Field outer, Field inner) throws ReflectiveOperationException {
        Object pair = outer.get(cube);
        return inner == null ? 0 : ((Number) inner.get(pair)).intValue();
    }

    private static PartDefinition findChild(PartDefinition root, String name) {
        if (name == null || name.isEmpty()) return root;
        for (String seg : name.split("/")) {
            PartDefinition next = root.getChild(seg);
            if (next == null) return null;
            root = next;
        }
        return root;
    }

    /** Concatenate borrowed/hybrid {@link BodyPart} arrays in declaration order (skips null / empty). */
    public static BodyPart[] concat(BodyPart[]... arrays) {
        int n = 0;
        if (arrays != null) {
            for (BodyPart[] a : arrays) {
                if (a != null) n += a.length;
            }
        }
        BodyPart[] out = new BodyPart[n];
        int i = 0;
        if (arrays != null) {
            for (BodyPart[] a : arrays) {
                if (a == null || a.length == 0) continue;
                System.arraycopy(a, 0, out, i, a.length);
                i += a.length;
            }
        }
        return out;
    }

    // ------------------------------------------------------------------ --
    // Reflective handles -- resolved once at class load.
    // ------------------------------------------------------------------ --

    private static final Field FIELD_LAYER_MESH;
    private static final Field FIELD_PART_CUBES;
    private static final Field FIELD_PART_POSE;
    private static final Field FIELD_CUBE_TEXCOORD;
    private static final Field FIELD_CUBE_ORIGIN;
    private static final Field FIELD_CUBE_DIM;
    private static final Field FIELD_CUBE_GROW;
    private static final Field FIELD_CUBE_MIRROR;
    private static final Field FIELD_UV_U;
    private static final Field FIELD_UV_V;

    static {
        try {
            FIELD_LAYER_MESH = makeAccessible(LayerDefinition.class.getDeclaredField("mesh"));

            FIELD_PART_CUBES = makeAccessible(PartDefinition.class.getDeclaredField("cubes"));
            FIELD_PART_POSE  = makeAccessible(PartDefinition.class.getDeclaredField("partPose"));

            Class<?> cubeDef = Class.forName("net.minecraft.client.model.geom.builders.CubeDefinition");
            FIELD_CUBE_ORIGIN   = makeAccessible(cubeDef.getDeclaredField("origin"));
            FIELD_CUBE_DIM      = makeAccessible(cubeDef.getDeclaredField("dimensions"));
            FIELD_CUBE_GROW     = makeAccessible(cubeDef.getDeclaredField("grow"));
            FIELD_CUBE_MIRROR   = makeAccessible(cubeDef.getDeclaredField("mirror"));
            FIELD_CUBE_TEXCOORD = makeAccessible(cubeDef.getDeclaredField("texCoord"));

            Class<?> uvPair = Class.forName("net.minecraft.client.model.geom.builders.UVPair");
            FIELD_UV_U = makeAccessibleOrNull(uvPair, "u");
            FIELD_UV_V = makeAccessibleOrNull(uvPair, "v");
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Necromancy: failed to wire BorrowedGeometry reflective fields", e);
        }
    }

    private static Field makeAccessible(Field f) { f.setAccessible(true); return f; }
    private static Field makeAccessibleOrNull(Class<?> c, String name) {
        try { return makeAccessible(c.getDeclaredField(name)); }
        catch (NoSuchFieldException e) { return null; }
    }
}
