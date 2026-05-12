package com.sirolf2009.necromancy.bodypart;

import com.sirolf2009.necromancy.Necromancy;
import com.sirolf2009.necromancy.api.BodyPartLocation;
import com.sirolf2009.necromancy.entity.EntityBodypartPreview;
import net.minecraft.Util;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Opt-in tracing for bodypart dev world preview flicker / jitter.
 * <p>
 * Enable with JVM flag on the <strong>game</strong> process:
 * {@code -Dnecromancy.debugBodypartPreview=true}
 * <p>
 * With Gradle, pass through from the daemon (example):
 * {@code ./gradlew runClient --no-daemon -Dnecromancy.debugBodypartPreview=true}
 * (works when {@code build.gradle} forwards the property — see {@code neoForge.runs.client}.)
 */
public final class BodypartPreviewDiagnostics {

    public static final boolean ENABLED =
        Boolean.parseBoolean(System.getProperty("necromancy.debugBodypartPreview", "false"));

    private static final ConcurrentHashMap<Integer, Long> NEXT_RENDER_LOG_MS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Integer, Long> NEXT_MESH_LOG_MS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Long> NEXT_RESOLVE_LOG_MS = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<Integer, Double> LAST_Y_INTERP = new ConcurrentHashMap<>();

    private BodypartPreviewDiagnostics() {}

    public static void logResolve(EntityBodypartPreview entity, String source, BodypartDefinition def) {
        if (!ENABLED) return;
        var id = entity.getPartIdRl();
        String logKey = id != null ? entity.getDevBlockPacked() + ":" + id : "packed:" + entity.getDevBlockPacked();
        long now = Util.getMillis();
        long next = NEXT_RESOLVE_LOG_MS.getOrDefault(logKey, 0L);
        if (now < next) return;
        NEXT_RESOLVE_LOG_MS.put(logKey, now + 400L);
        Necromancy.LOGGER.info(
            "[bodypartPreview] resolve key={} source={} defId={} visOffset=({}, {}, {}) hbOy={} hbSy={}",
            logKey, source, def.id(),
            fmt(def.visDx()), fmt(def.visDy()), fmt(def.visDz()),
            fmt(def.hbOy()), fmt(def.hbSy()));
    }

    public static void logRenderSample(EntityBodypartPreview e, float partialTicks, double yInterp, boolean showMesh) {
        if (!ENABLED) return;
        int id = e.getId();
        Double prev = LAST_Y_INTERP.get(id);
        double dy = prev == null ? 0 : yInterp - prev;
        LAST_Y_INTERP.put(id, yInterp);

        if (prev != null && Math.abs(dy) > 0.0005) {
            Necromancy.LOGGER.warn(
                "[bodypartPreview] Y_DELTA id={} tick={} pt={} yInterp={} dY={} showMesh={}",
                id, e.tickCount, partialTicks, fmt(yInterp), fmt(dy), showMesh);
        }

        long now = Util.getMillis();
        if (now < NEXT_RENDER_LOG_MS.getOrDefault(id, 0L)) return;
        NEXT_RENDER_LOG_MS.put(id, now + 100L);

        Necromancy.LOGGER.info(
            "[bodypartPreview] renderFrame id={} tick={} pt={} showMesh={} yInterp={} pos=({}, {}, {}) velY={} onGround={}",
            id, e.tickCount, partialTicks, showMesh,
            fmt(yInterp),
            fmt(e.getX()), fmt(e.getY()), fmt(e.getZ()),
            fmt(e.getDeltaMovement().y), e.onGround());
    }

    public static void logMeshRotations(
        String phase,
        String adapterMob,
        BodyPartLocation loc,
        boolean isolatePreviewMesh,
        float rootXr,
        float rootYr,
        float rootZr,
        int childCount,
        float child0xr,
        float child0yr,
        float child0zr
    ) {
        if (!ENABLED) return;
        long now = Util.getMillis();
        int k = Objects.hash(adapterMob, loc.name(), isolatePreviewMesh, phase);
        if (now < NEXT_MESH_LOG_MS.getOrDefault(k, 0L)) return;
        NEXT_MESH_LOG_MS.put(k, now + 100L);

        Necromancy.LOGGER.info(
            "[bodypartPreview] mesh {} adapter={} loc={} isolate={} rootRot=({}, {}, {}) children={} child0Rot=({}, {}, {})",
            phase, adapterMob, loc, isolatePreviewMesh,
            fmt(rootXr), fmt(rootYr), fmt(rootZr),
            childCount, fmt(child0xr), fmt(child0yr), fmt(child0zr));
    }

    private static String fmt(double v) {
        return Double.toString(Math.round(v * 100000.0) / 100000.0);
    }

    private static String fmt(float v) {
        return Float.toString(Math.round(v * 100000f) / 100000f);
    }
}
