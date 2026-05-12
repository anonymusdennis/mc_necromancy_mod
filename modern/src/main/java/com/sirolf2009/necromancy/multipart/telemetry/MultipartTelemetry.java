package com.sirolf2009.necromancy.multipart.telemetry;

import com.sirolf2009.necromancy.NecromancyConfig;
import com.sirolf2009.necromancy.multipart.broadphase.MultipartBroadphaseInstrumentation;
import net.minecraft.world.level.Level;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Central multipart profiling / telemetry — disabled by default via {@link NecromancyConfig#MULTIPART_TELEMETRY_ENABLED}.
 *
 * <p>Hooks short-circuit immediately when disabled so production builds incur effectively zero cost.
 * Use {@link NecromancyConfig#MULTIPART_TELEMETRY_STRESS_LIGHTWEIGHT} for multiplayer stress runs (aggregate counters only).
 *
 * <p>Also implements {@link MultipartBroadphaseInstrumentation} for wiring through {@link com.sirolf2009.necromancy.multipart.broadphase.MultipartBroadphaseRegistry}.
 */
public final class MultipartTelemetry implements MultipartBroadphaseInstrumentation {

    private static final MultipartTelemetry INSTANCE = new MultipartTelemetry();

    /** Unit tests may force enable/disable independent of config file. */
    private static volatile @Nullable Boolean profilingOverride;

    private static final class EntityCounters {
        final AtomicLong transformPropagations = new AtomicLong();
        final AtomicLong partSleepTransitions = new AtomicLong();
        final AtomicLong partWakeTransitions = new AtomicLong();
        final AtomicLong topologyCommits = new AtomicLong();
        final AtomicLong topologyRollbacks = new AtomicLong();
        final AtomicLong netPayloadBytesSent = new AtomicLong();
        final AtomicLong netPayloadBytesReceived = new AtomicLong();
    }

    /** Rough encoded payload body sizes (excluding vanilla packet framing). */
    private static final class NetApprox {
        static final int HEADER_SLACK = 16;
        static final int TOPOLOGY_NOTIFY = 36;
        static final int EDIT_LOCK = 44;
    }

    private final AtomicLong transformPropagationsTotal = new AtomicLong();

    private final AtomicLong broadphaseTickPublishCalls = new AtomicLong();
    private final AtomicLong broadphaseSnapshotCacheHits = new AtomicLong();
    private final AtomicLong broadphaseSnapshotRebuilds = new AtomicLong();
    private final AtomicLong broadphaseSnapshotRebuildNanos = new AtomicLong();

    private final AtomicLong topologyTxOpens = new AtomicLong();
    private final AtomicLong topologyTxCommits = new AtomicLong();
    private final AtomicLong topologyTxRollbacks = new AtomicLong();
    private final AtomicLong topologyTxOpenNanos = new AtomicLong();
    private final AtomicLong topologyTxCommitNanos = new AtomicLong();
    private final AtomicLong topologyTxRollbackNanos = new AtomicLong();

    private final AtomicLong partSleepTransitions = new AtomicLong();
    private final AtomicLong partWakeTransitions = new AtomicLong();

    private final AtomicLong overlapQueries = new AtomicLong();
    private final AtomicLong overlapCandidates = new AtomicLong();
    private final AtomicLong overlapNanos = new AtomicLong();

    private final AtomicLong rayQueries = new AtomicLong();
    private final AtomicLong rayBroadCandidates = new AtomicLong();
    private final AtomicLong rayNarrowHits = new AtomicLong();
    private final AtomicLong rayBroadNanos = new AtomicLong();
    private final AtomicLong rayNarrowNanos = new AtomicLong();

    private final AtomicLong netTxTransform = new AtomicLong();
    private final AtomicLong netTxTransformBytes = new AtomicLong();
    private final AtomicLong netRxTransform = new AtomicLong();
    private final AtomicLong netRxTransformBytes = new AtomicLong();

    private final AtomicLong netTxTopologyDelta = new AtomicLong();
    private final AtomicLong netTxTopologyDeltaBytes = new AtomicLong();
    private final AtomicLong netRxTopologyDelta = new AtomicLong();
    private final AtomicLong netRxTopologyDeltaBytes = new AtomicLong();

    private final AtomicLong netTxDamageSync = new AtomicLong();
    private final AtomicLong netTxDamageSyncBytes = new AtomicLong();
    private final AtomicLong netRxDamageSync = new AtomicLong();
    private final AtomicLong netRxDamageSyncBytes = new AtomicLong();

    private final AtomicLong netTxTopologyNotify = new AtomicLong();
    private final AtomicLong netTxTopologyNotifyBytes = new AtomicLong();
    private final AtomicLong netRxTopologyNotify = new AtomicLong();
    private final AtomicLong netRxTopologyNotifyBytes = new AtomicLong();

    private final AtomicLong netTxEditLock = new AtomicLong();
    private final AtomicLong netTxEditLockBytes = new AtomicLong();
    private final AtomicLong netRxEditLock = new AtomicLong();
    private final AtomicLong netRxEditLockBytes = new AtomicLong();

    private final ConcurrentHashMap<Integer, EntityCounters> byEntity = new ConcurrentHashMap<>();

    private MultipartTelemetry() {}

    public static MultipartBroadphaseInstrumentation broadphaseHooksOrNoop() {
        return isEnabled() ? INSTANCE : MultipartBroadphaseInstrumentation.noop();
    }

    public static boolean isEnabled() {
        Boolean o = profilingOverride;
        if (o != null) {
            return o;
        }
        return NecromancyConfig.MULTIPART_TELEMETRY_ENABLED.get();
    }

    public static boolean stressLightweight() {
        return NecromancyConfig.MULTIPART_TELEMETRY_STRESS_LIGHTWEIGHT.get();
    }

    @VisibleForTesting
    public static void testingSetProfilingOverride(@Nullable Boolean enabled) {
        profilingOverride = enabled;
    }

    public static void reset() {
        INSTANCE.doReset();
    }

    private void doReset() {
        transformPropagationsTotal.set(0);
        broadphaseTickPublishCalls.set(0);
        broadphaseSnapshotCacheHits.set(0);
        broadphaseSnapshotRebuilds.set(0);
        broadphaseSnapshotRebuildNanos.set(0);
        topologyTxOpens.set(0);
        topologyTxCommits.set(0);
        topologyTxRollbacks.set(0);
        topologyTxOpenNanos.set(0);
        topologyTxCommitNanos.set(0);
        topologyTxRollbackNanos.set(0);
        partSleepTransitions.set(0);
        partWakeTransitions.set(0);
        overlapQueries.set(0);
        overlapCandidates.set(0);
        overlapNanos.set(0);
        rayQueries.set(0);
        rayBroadCandidates.set(0);
        rayNarrowHits.set(0);
        rayBroadNanos.set(0);
        rayNarrowNanos.set(0);
        netTxTransform.set(0);
        netTxTransformBytes.set(0);
        netRxTransform.set(0);
        netRxTransformBytes.set(0);
        netTxTopologyDelta.set(0);
        netTxTopologyDeltaBytes.set(0);
        netRxTopologyDelta.set(0);
        netRxTopologyDeltaBytes.set(0);
        netTxDamageSync.set(0);
        netTxDamageSyncBytes.set(0);
        netRxDamageSync.set(0);
        netRxDamageSyncBytes.set(0);
        netTxTopologyNotify.set(0);
        netTxTopologyNotifyBytes.set(0);
        netRxTopologyNotify.set(0);
        netRxTopologyNotifyBytes.set(0);
        netTxEditLock.set(0);
        netTxEditLockBytes.set(0);
        netRxEditLock.set(0);
        netRxEditLockBytes.set(0);
        byEntity.clear();
    }

    private @Nullable EntityCounters entityOrNull(int entityId) {
        if (stressLightweight()) {
            return null;
        }
        return byEntity.computeIfAbsent(entityId, __ -> new EntityCounters());
    }

    public static void recordTransformPropagation(int multipartRootEntityId) {
        if (!isEnabled()) {
            return;
        }
        INSTANCE.transformPropagationsTotal.incrementAndGet();
        EntityCounters ec = INSTANCE.entityOrNull(multipartRootEntityId);
        if (ec != null) {
            ec.transformPropagations.incrementAndGet();
        }
    }

    public static void recordBroadphaseTickPublishInvocation(Level level) {
        if (!isEnabled() || level == null) {
            return;
        }
        INSTANCE.broadphaseTickPublishCalls.incrementAndGet();
    }

    public static void recordBroadphasePublishCached(Level level) {
        if (!isEnabled() || level == null) {
            return;
        }
        INSTANCE.broadphaseSnapshotCacheHits.incrementAndGet();
    }

    public static void recordBroadphasePublishRebuild(Level level, long rebuildNanos) {
        if (!isEnabled() || level == null) {
            return;
        }
        INSTANCE.broadphaseSnapshotRebuilds.incrementAndGet();
        INSTANCE.broadphaseSnapshotRebuildNanos.addAndGet(Math.max(0L, rebuildNanos));
    }

    public static void recordTopologyTxOpen(int entityId, long nanos) {
        if (!isEnabled()) {
            return;
        }
        INSTANCE.topologyTxOpens.incrementAndGet();
        INSTANCE.topologyTxOpenNanos.addAndGet(Math.max(0L, nanos));
    }

    public static void recordTopologyTxCommit(int entityId, long nanos) {
        if (!isEnabled()) {
            return;
        }
        INSTANCE.topologyTxCommits.incrementAndGet();
        INSTANCE.topologyTxCommitNanos.addAndGet(Math.max(0L, nanos));
        EntityCounters ec = INSTANCE.entityOrNull(entityId);
        if (ec != null) {
            ec.topologyCommits.incrementAndGet();
        }
    }

    public static void recordTopologyTxRollback(int entityId, long nanos) {
        if (!isEnabled()) {
            return;
        }
        INSTANCE.topologyTxRollbacks.incrementAndGet();
        INSTANCE.topologyTxRollbackNanos.addAndGet(Math.max(0L, nanos));
        EntityCounters ec = INSTANCE.entityOrNull(entityId);
        if (ec != null) {
            ec.topologyRollbacks.incrementAndGet();
        }
    }

    public static void recordPartSleepTransition(int multipartRootEntityId) {
        if (!isEnabled()) {
            return;
        }
        INSTANCE.partSleepTransitions.incrementAndGet();
        EntityCounters ec = INSTANCE.entityOrNull(multipartRootEntityId);
        if (ec != null) {
            ec.partSleepTransitions.incrementAndGet();
        }
    }

    public static void recordPartWakeTransition(int multipartRootEntityId) {
        if (!isEnabled()) {
            return;
        }
        INSTANCE.partWakeTransitions.incrementAndGet();
        EntityCounters ec = INSTANCE.entityOrNull(multipartRootEntityId);
        if (ec != null) {
            ec.partWakeTransitions.incrementAndGet();
        }
    }

    @Override
    public void recordOverlap(String scopeTag, long elapsedNanos, int candidatesEmitted) {
        if (!isEnabled()) {
            return;
        }
        overlapQueries.incrementAndGet();
        overlapCandidates.addAndGet(Math.max(0, candidatesEmitted));
        overlapNanos.addAndGet(Math.max(0L, elapsedNanos));
    }

    @Override
    public void recordRaySegment(String scopeTag, long broadphaseNanos, long narrowNanos,
                                 int broadphaseCandidates, int narrowHits) {
        if (!isEnabled()) {
            return;
        }
        rayQueries.incrementAndGet();
        rayBroadCandidates.addAndGet(Math.max(0, broadphaseCandidates));
        rayNarrowHits.addAndGet(Math.max(0, narrowHits));
        rayBroadNanos.addAndGet(Math.max(0L, broadphaseNanos));
        rayNarrowNanos.addAndGet(Math.max(0L, narrowNanos));
    }

    public static void recordNetSentTransformDelta(int entityId, byte[] packedOps) {
        if (!isEnabled()) {
            return;
        }
        int bytes = NetApprox.HEADER_SLACK + (packedOps == null ? 0 : packedOps.length);
        INSTANCE.netTxTransform.incrementAndGet();
        INSTANCE.netTxTransformBytes.addAndGet(bytes);
        EntityCounters ec = INSTANCE.entityOrNull(entityId);
        if (ec != null) {
            ec.netPayloadBytesSent.addAndGet(bytes);
        }
    }

    public static void recordNetReceivedTransformDelta(int entityId, byte[] packedOps) {
        if (!isEnabled()) {
            return;
        }
        int bytes = NetApprox.HEADER_SLACK + (packedOps == null ? 0 : packedOps.length);
        INSTANCE.netRxTransform.incrementAndGet();
        INSTANCE.netRxTransformBytes.addAndGet(bytes);
        EntityCounters ec = INSTANCE.entityOrNull(entityId);
        if (ec != null) {
            ec.netPayloadBytesReceived.addAndGet(bytes);
        }
    }

    public static void recordNetSentTopologyDelta(int entityId, byte[] packedTopologyOps) {
        if (!isEnabled()) {
            return;
        }
        int bytes = NetApprox.HEADER_SLACK + (packedTopologyOps == null ? 0 : packedTopologyOps.length);
        INSTANCE.netTxTopologyDelta.incrementAndGet();
        INSTANCE.netTxTopologyDeltaBytes.addAndGet(bytes);
        EntityCounters ec = INSTANCE.entityOrNull(entityId);
        if (ec != null) {
            ec.netPayloadBytesSent.addAndGet(bytes);
        }
    }

    public static void recordNetReceivedTopologyDelta(int entityId, byte[] packedTopologyOps) {
        if (!isEnabled()) {
            return;
        }
        int bytes = NetApprox.HEADER_SLACK + (packedTopologyOps == null ? 0 : packedTopologyOps.length);
        INSTANCE.netRxTopologyDelta.incrementAndGet();
        INSTANCE.netRxTopologyDeltaBytes.addAndGet(bytes);
        EntityCounters ec = INSTANCE.entityOrNull(entityId);
        if (ec != null) {
            ec.netPayloadBytesReceived.addAndGet(bytes);
        }
    }

    public static void recordNetSentDamageSync(int entityId, byte[] packedDamage) {
        if (!isEnabled()) {
            return;
        }
        int bytes = NetApprox.HEADER_SLACK + (packedDamage == null ? 0 : packedDamage.length);
        INSTANCE.netTxDamageSync.incrementAndGet();
        INSTANCE.netTxDamageSyncBytes.addAndGet(bytes);
        EntityCounters ec = INSTANCE.entityOrNull(entityId);
        if (ec != null) {
            ec.netPayloadBytesSent.addAndGet(bytes);
        }
    }

    public static void recordNetReceivedDamageSync(int entityId, byte[] packedDamage) {
        if (!isEnabled()) {
            return;
        }
        int bytes = NetApprox.HEADER_SLACK + (packedDamage == null ? 0 : packedDamage.length);
        INSTANCE.netRxDamageSync.incrementAndGet();
        INSTANCE.netRxDamageSyncBytes.addAndGet(bytes);
        EntityCounters ec = INSTANCE.entityOrNull(entityId);
        if (ec != null) {
            ec.netPayloadBytesReceived.addAndGet(bytes);
        }
    }

    public static void recordNetSentTopologyNotify(int entityId) {
        if (!isEnabled()) {
            return;
        }
        int bytes = NetApprox.TOPOLOGY_NOTIFY;
        INSTANCE.netTxTopologyNotify.incrementAndGet();
        INSTANCE.netTxTopologyNotifyBytes.addAndGet(bytes);
        EntityCounters ec = INSTANCE.entityOrNull(entityId);
        if (ec != null) {
            ec.netPayloadBytesSent.addAndGet(bytes);
        }
    }

    public static void recordNetReceivedTopologyNotify(int entityId) {
        if (!isEnabled()) {
            return;
        }
        int bytes = NetApprox.TOPOLOGY_NOTIFY;
        INSTANCE.netRxTopologyNotify.incrementAndGet();
        INSTANCE.netRxTopologyNotifyBytes.addAndGet(bytes);
        EntityCounters ec = INSTANCE.entityOrNull(entityId);
        if (ec != null) {
            ec.netPayloadBytesReceived.addAndGet(bytes);
        }
    }

    public static void recordNetSentEditLock(int entityId) {
        if (!isEnabled()) {
            return;
        }
        int bytes = NetApprox.EDIT_LOCK;
        INSTANCE.netTxEditLock.incrementAndGet();
        INSTANCE.netTxEditLockBytes.addAndGet(bytes);
        EntityCounters ec = INSTANCE.entityOrNull(entityId);
        if (ec != null) {
            ec.netPayloadBytesSent.addAndGet(bytes);
        }
    }

    public static void recordNetReceivedEditLock(int entityId) {
        if (!isEnabled()) {
            return;
        }
        int bytes = NetApprox.EDIT_LOCK;
        INSTANCE.netRxEditLock.incrementAndGet();
        INSTANCE.netRxEditLockBytes.addAndGet(bytes);
        EntityCounters ec = INSTANCE.entityOrNull(entityId);
        if (ec != null) {
            ec.netPayloadBytesReceived.addAndGet(bytes);
        }
    }

    /**
     * HUD / F3 lines — stable ordering for screenshots and log pastes.
     */
    public static List<String> formatAggregateLines() {
        if (!isEnabled()) {
            return List.of("[multipart telemetry OFF — necro-common.toml multipart.enableMultipartTelemetry=false]");
        }
        MultipartTelemetry t = INSTANCE;
        List<String> lines = new ArrayList<>(24);
        lines.add(stressLightweight()
            ? "[multipart telemetry: LIGHTWEIGHT aggregate mode]"
            : "[multipart telemetry: full (per-entity buckets enabled)]");
        lines.add(("xf_propagations=%d  broadphase_tick_publish=%d  snapshot_hit=%d  snapshot_rebuild=%d  rebuild_ns(sum)=%d")
            .formatted(t.transformPropagationsTotal.get(), t.broadphaseTickPublishCalls.get(),
                t.broadphaseSnapshotCacheHits.get(), t.broadphaseSnapshotRebuilds.get(),
                t.broadphaseSnapshotRebuildNanos.get()));
        lines.add(("topology_tx open=%d commit=%d rollback=%d | ns(open,commit,rollback)=%d,%d,%d")
            .formatted(t.topologyTxOpens.get(), t.topologyTxCommits.get(), t.topologyTxRollbacks.get(),
                t.topologyTxOpenNanos.get(), t.topologyTxCommitNanos.get(), t.topologyTxRollbackNanos.get()));
        lines.add(("activity part_sleep=%d part_wake=%d")
            .formatted(t.partSleepTransitions.get(), t.partWakeTransitions.get()));
        lines.add(("query overlap_q=%d overlap_cand=%d overlap_ns=%d | ray_q=%d ray_cand=%d ray_hit=%d ray_bf_ns=%d ray_narrow_ns=%d")
            .formatted(t.overlapQueries.get(), t.overlapCandidates.get(), t.overlapNanos.get(),
                t.rayQueries.get(), t.rayBroadCandidates.get(), t.rayNarrowHits.get(),
                t.rayBroadNanos.get(), t.rayNarrowNanos.get()));
        lines.add(("net_tx xf=%d/%dB topo=%d/%dB dmg=%d/%dB notify=%d/%dB lock=%d/%dB")
            .formatted(t.netTxTransform.get(), t.netTxTransformBytes.get(),
                t.netTxTopologyDelta.get(), t.netTxTopologyDeltaBytes.get(),
                t.netTxDamageSync.get(), t.netTxDamageSyncBytes.get(),
                t.netTxTopologyNotify.get(), t.netTxTopologyNotifyBytes.get(),
                t.netTxEditLock.get(), t.netTxEditLockBytes.get()));
        lines.add(("net_rx xf=%d/%dB topo=%d/%dB dmg=%d/%dB notify=%d/%dB lock=%d/%dB")
            .formatted(t.netRxTransform.get(), t.netRxTransformBytes.get(),
                t.netRxTopologyDelta.get(), t.netRxTopologyDeltaBytes.get(),
                t.netRxDamageSync.get(), t.netRxDamageSyncBytes.get(),
                t.netRxTopologyNotify.get(), t.netRxTopologyNotifyBytes.get(),
                t.netRxEditLock.get(), t.netRxEditLockBytes.get()));
        lines.add("net payload sizes are approximate (blob + header slack); buckets exclude vanilla framing.");
        return lines;
    }

    public static List<String> formatPerEntityLines(int entityId, int maxLines) {
        EntityCounters ec = INSTANCE.byEntity.get(entityId);
        List<String> out = new ArrayList<>();
        if (ec == null) {
            out.add("entity " + entityId + ": no multipart telemetry samples yet");
            return out;
        }
        out.add(("entity %d: xf=%d sleep=%d wake=%d topo_commit=%d topo_rb=%d net_tx_B=%d net_rx_B=%d")
            .formatted(entityId,
                ec.transformPropagations.get(),
                ec.partSleepTransitions.get(),
                ec.partWakeTransitions.get(),
                ec.topologyCommits.get(),
                ec.topologyRollbacks.get(),
                ec.netPayloadBytesSent.get(),
                ec.netPayloadBytesReceived.get()));
        return out.subList(0, Math.min(out.size(), maxLines));
    }

    /**
     * Snapshot entity ids that currently have non-empty per-entity buckets (debug listing).
     */
    public static List<Integer> trackedEntityIdsSnapshot() {
        return List.copyOf(INSTANCE.byEntity.keySet());
    }

    public static String dimensionTag(Level level) {
        if (level == null) {
            return "?";
        }
        ResourceLocation key = level.dimension().location();
        return key.getNamespace() + ":" + key.getPath();
    }

    @VisibleForTesting
    static long testingTransformPropagations() {
        return INSTANCE.transformPropagationsTotal.get();
    }

    @VisibleForTesting
    static long testingBroadphaseRebuilds() {
        return INSTANCE.broadphaseSnapshotRebuilds.get();
    }
}
