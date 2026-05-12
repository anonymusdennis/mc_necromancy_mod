package com.sirolf2009.necromancy.multipart.editor.session;

import com.sirolf2009.necromancy.multipart.MultipartHierarchyMemento;
import com.sirolf2009.necromancy.multipart.RootMobEntity;
import com.sirolf2009.necromancy.multipart.broadphase.MultipartBroadphaseHooks;
import com.sirolf2009.necromancy.multipart.telemetry.MultipartTelemetry;
import com.sirolf2009.necromancy.multipart.broadphase.MultipartBroadphaseRegistry;
import com.sirolf2009.necromancy.multipart.broadphase.MultipartBroadphaseWorld;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Authoritative server-side topology lease + nested transactional snapshots for multipart entities.
 *
 * <p><strong>Preview vs committed:</strong> cosmetic previews belong on the {@linkplain com.sirolf2009.necromancy.multipart.part.BodyPartNode#renderOverlayTransform()
 * render overlay lane} (client-only helpers). This service guards {@linkplain com.sirolf2009.necromancy.multipart.TransformHierarchy simulation topology}
 * mutations that must replicate.
 *
 * <p>Nested transactions push rollback anchors (LIFO). {@link #commitTransaction(ServerLevel, int)} discards the newest anchor without restoring;
 * {@link #rollbackTransaction(ServerLevel, int)} applies it.
 *
 * <p>After rollback / release, callers should fan out {@link com.sirolf2009.necromancy.multipart.network.payload.MultipartTopologyNotifyPayload}
 * (or entity data sync) so remote clients observe restored revision counters.
 */
public final class MultipartServerTopologyEditService {

    private static final AtomicLong SESSION_GEN = new AtomicLong(1L);

    private static final ConcurrentHashMap<SessionKey, Object> STRIPES = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<SessionKey, Lease> LEASES = new ConcurrentHashMap<>();

    private MultipartServerTopologyEditService() {}

    public record SessionKey(ResourceLocation dimension, int entityId) {
        public static SessionKey of(ServerLevel level, int entityId) {
            return new SessionKey(level.dimension().location(), entityId);
        }
    }

    private static Object stripe(SessionKey k) {
        return STRIPES.computeIfAbsent(k, __ -> new Object());
    }

    private static final class Lease {
        UUID holderUuid;
        final long sessionGeneration = SESSION_GEN.incrementAndGet();
        int txDepth;
        /** Newest snapshot at the tail — mirrors {@link #txDepth} when consistent. */
        final Deque<MultipartHierarchyMemento> rollbackSnapshots = new ArrayDeque<>();
        final MultipartTopologyOperationJournal journal = new MultipartTopologyOperationJournal();
    }

    /**
     * Acquires an exclusive topology lease or confirms {@code holder} already owns it.
     */
    public static MultipartTopologyEditResult acquireLock(ServerLevel level, LivingEntity entity, ServerPlayer holder) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(holder, "holder");
        if (!(entity instanceof RootMobEntity)) {
            return MultipartTopologyEditResult.deny(MultipartTopologyEditResult.RC_NOT_MULTIPART);
        }
        if (entity.level() != level) {
            return MultipartTopologyEditResult.deny(MultipartTopologyEditResult.RC_WRONG_DIMENSION);
        }
        SessionKey key = SessionKey.of(level, entity.getId());
        synchronized (stripe(key)) {
            Lease lease = LEASES.get(key);
            if (lease == null) {
                Lease nu = new Lease();
                nu.holderUuid = holder.getUUID();
                nu.txDepth = 0;
                LEASES.put(key, nu);
                broadcastLock(level, entity, nu);
                return MultipartTopologyEditResult.success();
            }
            if (lease.holderUuid.equals(holder.getUUID())) {
                return MultipartTopologyEditResult.success();
            }
            return MultipartTopologyEditResult.deny(MultipartTopologyEditResult.RC_LOCK_HELD_BY_OTHER);
        }
    }

    /** Releases the lease when {@code holder} is the owner — pending transactions roll back first. */
    public static MultipartTopologyEditResult releaseLock(ServerLevel level, LivingEntity entity, ServerPlayer holder) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(holder, "holder");
        if (!(entity instanceof RootMobEntity r)) {
            return MultipartTopologyEditResult.deny(MultipartTopologyEditResult.RC_NOT_MULTIPART);
        }
        SessionKey key = SessionKey.of(level, entity.getId());
        MultipartBroadphaseWorld bw = MultipartBroadphaseRegistry.get(level);
        boolean closedOuterTopologyTx = false;
        synchronized (stripe(key)) {
            Lease lease = LEASES.get(key);
            if (lease == null) {
                return MultipartTopologyEditResult.success();
            }
            if (!lease.holderUuid.equals(holder.getUUID())) {
                return MultipartTopologyEditResult.deny(MultipartTopologyEditResult.RC_WRONG_PLAYER);
            }
            bw.clearDeferredSlotsForEntity(entity.getId());
            closedOuterTopologyTx = lease.txDepth > 0;
            unwindRollbackSnapshots(r, lease);
            lease.journal.clear();
            LEASES.remove(key);
            STRIPES.remove(key);
            broadcastUnlock(level, entity);
        }
        if (closedOuterTopologyTx) {
            MultipartBroadphaseHooks.endDeferredBroadphasePublish(level);
        }
        bw.updateFromRoot(r);
        return MultipartTopologyEditResult.success();
    }

    /**
     * Opens / nests a topology transaction — each {@code open} captures a hierarchy snapshot before mutations.
     */
    public static MultipartTopologyTransaction openTransaction(ServerLevel level, LivingEntity entity, ServerPlayer holder) {
        MultipartTopologyEditResult gate = ensureLease(level, entity, holder);
        if (!gate.ok()) {
            throw new IllegalStateException("Cannot open multipart topology transaction: " + gate.reasonCode());
        }
        SessionKey key = SessionKey.of(level, entity.getId());
        long t0 = System.nanoTime();
        synchronized (stripe(key)) {
            Lease lease = Objects.requireNonNull(LEASES.get(key));
            lease.txDepth++;
            lease.rollbackSnapshots.addLast(MultipartHierarchyMemento.capture(((RootMobEntity) entity).multipartHierarchy()));
            if (lease.txDepth == 1) {
                MultipartBroadphaseHooks.beginDeferredBroadphasePublish(level);
            }
        }
        MultipartTelemetry.recordTopologyTxOpen(entity.getId(), System.nanoTime() - t0);
        return new MultipartTopologyTransaction(level, entity.getId());
    }

    public static void commitTransaction(ServerLevel level, int entityId) {
        SessionKey key = SessionKey.of(level, entityId);
        boolean shouldEndDefer = false;
        long t0 = System.nanoTime();
        synchronized (stripe(key)) {
            Lease lease = LEASES.get(key);
            if (lease == null || lease.txDepth <= 0 || lease.rollbackSnapshots.isEmpty()) {
                return;
            }
            lease.rollbackSnapshots.pollLast();
            lease.txDepth--;
            shouldEndDefer = lease.txDepth == 0;
            if (shouldEndDefer) {
                lease.journal.clear();
            }
        }
        MultipartTelemetry.recordTopologyTxCommit(entityId, System.nanoTime() - t0);
        if (shouldEndDefer) {
            MultipartBroadphaseHooks.endDeferredBroadphasePublish(level);
        }
    }

    public static void rollbackTransaction(ServerLevel level, int entityId) {
        Entity entity = level.getEntity(entityId);
        if (!(entity instanceof RootMobEntity r)) {
            return;
        }
        SessionKey key = SessionKey.of(level, entityId);
        MultipartBroadphaseWorld bw = MultipartBroadphaseRegistry.get(level);
        boolean shouldEndDefer = false;
        long t0 = System.nanoTime();
        synchronized (stripe(key)) {
            Lease lease = LEASES.get(key);
            if (lease == null || lease.rollbackSnapshots.isEmpty()) {
                return;
            }
            bw.clearDeferredSlotsForEntity(entityId);
            MultipartHierarchyMemento snap = lease.rollbackSnapshots.pollLast();
            snap.applyTo(r.multipartHierarchy());
            if (lease.txDepth > 0) {
                lease.txDepth--;
            }
            shouldEndDefer = lease.txDepth == 0;
            if (shouldEndDefer) {
                lease.journal.clear();
            }
        }
        MultipartTelemetry.recordTopologyTxRollback(entityId, System.nanoTime() - t0);
        if (shouldEndDefer) {
            MultipartBroadphaseHooks.endDeferredBroadphasePublish(level);
        }
        bw.updateFromRoot(r);
    }

    /** Records an operation-table entry (optional — invoked by orchestrators implementing {@link MultipartRecordedTopologyOp}). */
    public static MultipartTopologyEditResult recordOperation(ServerLevel level, LivingEntity entity, ServerPlayer holder,
                                                             MultipartRecordedTopologyOp op) {
        MultipartTopologyEditResult gate = ensureLease(level, entity, holder);
        if (!gate.ok()) {
            return gate;
        }
        SessionKey key = SessionKey.of(level, entity.getId());
        synchronized (stripe(key)) {
            Lease lease = Objects.requireNonNull(LEASES.get(key));
            lease.journal.append(op);
        }
        return MultipartTopologyEditResult.success();
    }

    public static MultipartTopologyEditResult ensureLease(ServerLevel level, LivingEntity entity, ServerPlayer holder) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(holder, "holder");
        if (!(entity instanceof RootMobEntity)) {
            return MultipartTopologyEditResult.deny(MultipartTopologyEditResult.RC_NOT_MULTIPART);
        }
        if (entity.level() != level) {
            return MultipartTopologyEditResult.deny(MultipartTopologyEditResult.RC_WRONG_DIMENSION);
        }
        SessionKey key = SessionKey.of(level, entity.getId());
        synchronized (stripe(key)) {
            Lease lease = LEASES.get(key);
            if (lease == null) {
                return MultipartTopologyEditResult.deny(MultipartTopologyEditResult.RC_LOCK_REQUIRED);
            }
            if (!lease.holderUuid.equals(holder.getUUID())) {
                return MultipartTopologyEditResult.deny(MultipartTopologyEditResult.RC_WRONG_PLAYER);
            }
            return MultipartTopologyEditResult.success();
        }
    }

    /** Cancels session + lease with structural rollback when needed (multiplayer-safe abandon flow). */
    public static MultipartTopologyEditResult cancelSession(ServerLevel level, LivingEntity entity, ServerPlayer holder) {
        return releaseLock(level, entity, holder);
    }

    public static void clearEntity(ServerLevel level, int entityId) {
        SessionKey key = SessionKey.of(level, entityId);
        Entity entity = level.getEntity(entityId);
        MultipartBroadphaseWorld bw = MultipartBroadphaseRegistry.get(level);
        boolean shouldEndDefer = false;
        synchronized (stripe(key)) {
            Lease lease = LEASES.remove(key);
            if (lease != null) {
                STRIPES.remove(key);
                bw.clearDeferredSlotsForEntity(entityId);
                shouldEndDefer = lease.txDepth > 0;
                if (entity != null) {
                    broadcastUnlock(level, entity);
                }
            }
        }
        if (shouldEndDefer) {
            MultipartBroadphaseHooks.endDeferredBroadphasePublish(level);
        }
    }

    public static void releaseAllForPlayer(ServerLevel level, UUID playerUuid) {
        MultipartBroadphaseWorld bw = MultipartBroadphaseRegistry.get(level);
        ArrayList<SessionKey> keys = new ArrayList<>(LEASES.keySet());
        for (SessionKey key : keys) {
            if (!key.dimension.equals(level.dimension().location())) {
                continue;
            }
            boolean hadTopologyTx = false;
            RootMobEntity rootRefresh = null;
            synchronized (stripe(key)) {
                Lease lease = LEASES.get(key);
                if (lease == null || !lease.holderUuid.equals(playerUuid)) {
                    continue;
                }
                Entity entity = level.getEntity(key.entityId);
                bw.clearDeferredSlotsForEntity(key.entityId);
                hadTopologyTx = lease.txDepth > 0;
                if (entity instanceof RootMobEntity r) {
                    unwindRollbackSnapshots(r, lease);
                    rootRefresh = r;
                } else {
                    lease.rollbackSnapshots.clear();
                    lease.txDepth = 0;
                }
                lease.journal.clear();
                LEASES.remove(key);
                STRIPES.remove(key);
                if (entity != null) {
                    broadcastUnlock(level, entity);
                }
            }
            if (hadTopologyTx) {
                MultipartBroadphaseHooks.endDeferredBroadphasePublish(level);
            }
            if (rootRefresh != null) {
                bw.updateFromRoot(rootRefresh);
            }
        }
    }

    /** Single-line debug probe for commands / logs (server-side). */
    public static String describe(ServerLevel level, int entityId) {
        SessionKey key = SessionKey.of(level, entityId);
        synchronized (stripe(key)) {
            Lease lease = LEASES.get(key);
            if (lease == null) {
                return "multipart edit: entity %d unlocked".formatted(entityId);
            }
            return ("multipart edit: entity %d locked holder=%s session=%d txDepth=%d rollback_stack=%d journaling=%d ops")
                .formatted(entityId, lease.holderUuid, lease.sessionGeneration, lease.txDepth,
                    lease.rollbackSnapshots.size(), lease.journal.viewOps().size());
        }
    }

    private static void unwindRollbackSnapshots(RootMobEntity root, Lease lease) {
        while (!lease.rollbackSnapshots.isEmpty()) {
            lease.rollbackSnapshots.pollLast().applyTo(root.multipartHierarchy());
        }
        lease.txDepth = 0;
    }

    private static void broadcastLock(ServerLevel level, LivingEntity entity, Lease lease) {
        if (!(entity instanceof RootMobEntity r) || !r.multipartEditLocksBroadcastToClients()) {
            return;
        }
        var payload = new com.sirolf2009.necromancy.multipart.network.payload.MultipartEditLockNotifyPayload(
            entity.getId(), true, lease.holderUuid, lease.sessionGeneration);
        MultipartTelemetry.recordNetSentEditLock(entity.getId());
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, payload);
    }

    private static void broadcastUnlock(ServerLevel level, Entity entity) {
        if (!(entity instanceof LivingEntity living)) return;
        if (!(living instanceof RootMobEntity r) || !r.multipartEditLocksBroadcastToClients()) {
            return;
        }
        var payload = new com.sirolf2009.necromancy.multipart.network.payload.MultipartEditLockNotifyPayload(
            entity.getId(), false, null, 0L);
        MultipartTelemetry.recordNetSentEditLock(entity.getId());
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(living, payload);
    }
}
