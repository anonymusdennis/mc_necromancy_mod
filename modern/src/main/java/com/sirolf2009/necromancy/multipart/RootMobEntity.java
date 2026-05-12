package com.sirolf2009.necromancy.multipart;

import com.sirolf2009.necromancy.multipart.animation.MultipartAnimationFrame;
import com.sirolf2009.necromancy.multipart.broadphase.MultipartActivityConfig;
import com.sirolf2009.necromancy.multipart.broadphase.MultipartBroadphaseHooks;
import com.sirolf2009.necromancy.multipart.math.PartTransform;
import com.sirolf2009.necromancy.multipart.math.QuaternionOps;
import com.sirolf2009.necromancy.multipart.network.MultipartReplicationBridge;
import com.sirolf2009.necromancy.multipart.network.payload.MultipartTopologyDeltaPayload;
import com.sirolf2009.necromancy.multipart.part.BodyPartNode;
import com.sirolf2009.necromancy.multipart.telemetry.MultipartTelemetry;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

/**
 * Single authoritative {@link LivingEntity} owning a multipart {@link TransformHierarchy}.
 */
public interface RootMobEntity {

    LivingEntity asMultipartRoot();

    TransformHierarchy multipartHierarchy();

    default Vec3 multipartPivot() {
        return asMultipartRoot().position();
    }

    /** Full body orientation (yaw/pitch/roll) used for simulation propagation. */
    default Quaternionf multipartBodyOrientation() {
        LivingEntity e = asMultipartRoot();
        return QuaternionOps.fromYawPitchRollDegrees(e.getYRot(), e.getXRot(), multipartBodyRollDegrees());
    }

    /** Extra roll around body forward axis (degrees); hooks for banking flight / knockdown crawl poses. */
    default float multipartBodyRollDegrees() {
        return 0f;
    }

    /** Cosmetic / animation-only root overlay (applied after systemic bone layers). Implementations must return a dedicated {@link PartTransform} owned by the
     * entity (or a fresh instance per call); returning a shared static singleton would allow unintentional mutation.
     */
    default PartTransform multipartRootRenderOverlay() {
        return PartTransform.identity();
    }

    /**
     * When true, captures previous simulation poses and composed render overlays before each propagation pass for
     * {@link com.sirolf2009.necromancy.multipart.interpolation.MultipartPoseInterpolator}.
     */
    default boolean multipartPoseInterpolationCapture() {
        return false;
    }

    /** Forces every node to recompute transforms (expensive validation mode). */
    default boolean multipartFullTransformValidation() {
        return false;
    }

    /** When true, {@link com.sirolf2009.necromancy.multipart.damage.MultipartDamageRouter#hurtFromPart} writes {@link com.sirolf2009.necromancy.multipart.damage.PartDamageState}. */
    default boolean multipartUsesPerPartHealth() {
        return false;
    }

    /**
     * When true, {@link MultipartBroadphaseHooks#afterMultipartTick(RootMobEntity)} publishes bounds after each
     * {@link #multipartTick()} / {@link #multipartTickYawLegacy()}.
     */
    default boolean multipartBroadphaseAutoPublish() {
        return false;
    }

    /**
     * Single aggregate AABB per entity in the spatial hash (minimal work for idle crowds); narrow-phase still uses full
     * hierarchy when invoked.
     */
    default boolean multipartBroadphaseDormant() {
        return false;
    }

    /**
     * When true, topology lease changes broadcast {@link com.sirolf2009.necromancy.multipart.network.payload.MultipartEditLockNotifyPayload}
     * for clients (HUD / collaborative tooling).
     */
    default boolean multipartEditLocksBroadcastToClients() {
        return true;
    }

    /** Quiet thresholds / epsilon gates for {@link com.sirolf2009.necromancy.multipart.broadphase.MultipartActivityGovernor}. */
    default MultipartActivityConfig multipartActivityConfig() {
        return MultipartActivityConfig.DEFAULT;
    }

    /** When false, activity stays ACTIVE-equivalent for broad-phase without per-tick governor work. */
    default boolean multipartActivityGovernorEnabled() {
        return true;
    }

    /** If a part reaches zero HP, mark severed for detach pipelines / loot. */
    default boolean multipartSeverPartWhenDestroyed() {
        return false;
    }

    /** Additional vanilla damage after part absorbs {@code appliedToPart} (policy hook). */
    default float multipartResidualDamageToVanilla(float rawIncoming, float appliedToPart, BodyPartNode part, DamageSource source) {
        return 0f;
    }

    /**
     * Push weighted bone-local overlays ({@link com.sirolf2009.necromancy.multipart.animation.WeightedPartTransform})
     * for this tick. Simulation poses remain authoritative unless callers mutate simulation locals separately.
     */
    default void multipartCollectAnimationLayers(MultipartAnimationFrame frame) {
    }

    // --- Networking hooks (server pushes deltas; clients mirror authoritative assembly). -----------------

    default void multipartConsumeTopologyNotify(int topologyRevision, long transformDirtyRevision, long propagationSerial) {
    }

    /** Sparse transforms — {@link com.sirolf2009.necromancy.multipart.network.binary.MultipartTransformDeltaBinary}. */
    default void multipartConsumeTransformDelta(com.sirolf2009.necromancy.multipart.network.payload.MultipartTransformDeltaPayload payload) {
        MultipartReplicationBridge.applyTransformDeltaPayload(this, payload);
    }

    /** Sparse damage — {@link com.sirolf2009.necromancy.multipart.network.binary.MultipartDamageDeltaBinary}. */
    default void multipartConsumeDamageSync(com.sirolf2009.necromancy.multipart.network.payload.MultipartDamageSyncPayload payload) {
        MultipartReplicationBridge.applyDamagePayload(this, payload);
    }

    /** Socket topology edits — {@link com.sirolf2009.necromancy.multipart.network.binary.MultipartTopologyDeltaBinary}. */
    default void multipartConsumeTopologyDelta(MultipartTopologyDeltaPayload payload) {
        MultipartReplicationBridge.applyTopologyDeltaPayload(this, payload);
    }

    default void multipartTick() {
        TransformHierarchy h = multipartHierarchy();
        long serialBefore = h.propagationSerial();
        if (multipartPoseInterpolationCapture()) {
            for (BodyPartNode n : h.nodes()) {
                n.setPreviousSimulationWorldPose(n.simulationWorldPose());
                n.snapshotPreviousComposedRenderOverlayForInterpolation();
            }
        }
        MultipartAnimationFrame frame = new MultipartAnimationFrame();
        multipartCollectAnimationLayers(frame);
        LivingEntity e = asMultipartRoot();
        h.tick(e, multipartPivot(), multipartBodyOrientation(), multipartRootRenderOverlay(), multipartFullTransformValidation(), frame);
        if (multipartActivityGovernorEnabled()) {
            h.activityGovernor().tick(this);
        }
        if (h.propagationSerial() != serialBefore) {
            MultipartTelemetry.recordTransformPropagation(e.getId());
        }
        MultipartBroadphaseHooks.afterMultipartTick(this);
    }

    /** Legacy yaw-only multipart tick (pitch pulled from entity {@code getXRot()}). */
    default void multipartTickYawLegacy() {
        TransformHierarchy h = multipartHierarchy();
        long serialBefore = h.propagationSerial();
        if (multipartPoseInterpolationCapture()) {
            for (BodyPartNode n : h.nodes()) {
                n.setPreviousSimulationWorldPose(n.simulationWorldPose());
                n.snapshotPreviousComposedRenderOverlayForInterpolation();
            }
        }
        MultipartAnimationFrame frame = new MultipartAnimationFrame();
        multipartCollectAnimationLayers(frame);
        LivingEntity e = asMultipartRoot();
        Quaternionf orient = QuaternionOps.fromYawPitchRollDegrees(e.getYRot(), e.getXRot(), 0f);
        h.tick(e, multipartPivot(), orient, multipartRootRenderOverlay(), multipartFullTransformValidation(), frame);
        if (multipartActivityGovernorEnabled()) {
            h.activityGovernor().tick(this);
        }
        if (h.propagationSerial() != serialBefore) {
            MultipartTelemetry.recordTransformPropagation(e.getId());
        }
        MultipartBroadphaseHooks.afterMultipartTick(this);
    }
}
