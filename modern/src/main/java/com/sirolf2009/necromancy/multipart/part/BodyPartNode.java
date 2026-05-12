package com.sirolf2009.necromancy.multipart.part;

import com.sirolf2009.necromancy.multipart.WorldPose;
import com.sirolf2009.necromancy.multipart.collision.CollisionResolve;
import com.sirolf2009.necromancy.multipart.collision.OrientedBounds;
import com.sirolf2009.necromancy.multipart.collision.ResolvedObb;
import com.sirolf2009.necromancy.multipart.debug.TransformAliasingAssertions;
import com.sirolf2009.necromancy.multipart.damage.PartDamageState;
import com.sirolf2009.necromancy.multipart.damage.PartFunctionalFlag;
import com.sirolf2009.necromancy.multipart.broadphase.MultipartPartActivityState;
import com.sirolf2009.necromancy.multipart.math.PartTransform;
import com.sirolf2009.necromancy.multipart.math.QuaternionOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Bodypart node: authoritative simulation TRS, layered render overlays (systemic layers + editor lane), cached world poses,
 * oriented collision, damage state.
 */
public final class BodyPartNode {

    private final ResourceLocation id;
    private final HitboxComponent hitbox;

    private final PartTransform simulationLocal = new PartTransform();
    /**
     * Cosmetic / editor overlay (applied last inside the render blend pipeline after systemic animation layers).
     * Authoritative simulation uses {@link #simulationLocalTransform()} only.
     */
    private final PartTransform renderOverlayLocal = new PartTransform();

    private final PartTransform composedRenderOverlayLocal = new PartTransform();
    private final PartTransform previousComposedRenderOverlayLocal = new PartTransform();
    private boolean hasPreviousComposedRenderOverlay;

    private @Nullable ResourceLocation parentId;

    private final List<AttachmentPoint> attachmentPoints = new ArrayList<>();

    private boolean attachedToParent = true;

    private MultipartPartActivityState partActivityState = MultipartPartActivityState.ACTIVE;
    private int activityQuietTicks;

    /**
     * When true, only this node's bound is indexed for broad-phase; descendants are omitted (rigid welded subtree /
     * manually enlarged proxy bounds).
     */
    private boolean broadphaseSubtreeProxyOnly;

    private WorldPose simulationWorld = WorldPose.identity();
    private WorldPose renderWorld = WorldPose.identity();

    private @Nullable WorldPose previousSimulationWorld;
    private ResolvedObb simulationCollisionObb =
        new ResolvedObb(Vec3.ZERO, QuaternionOps.fromYawDegrees(0f), new Vector3f(1e-4f, 1e-4f, 1e-4f));
    private AABB simulationBroadphase = deadHitbox();

    private final PartDamageState damageState = new PartDamageState();

    public BodyPartNode(ResourceLocation id, HitboxComponent hitbox) {
        this.id = Objects.requireNonNull(id);
        this.hitbox = Objects.requireNonNull(hitbox);
    }

    public ResourceLocation id() {
        return id;
    }

    public HitboxComponent hitbox() {
        return hitbox;
    }

    public PartTransform simulationLocalTransform() {
        return simulationLocal;
    }

    public PartTransform renderOverlayTransform() {
        return renderOverlayLocal;
    }

    /** Full local overlay applied after simulation (layers + editor merge); safe for render interpolation. */
    public void composedRenderOverlayInto(PartTransform dest) {
        dest.set(composedRenderOverlayLocal);
    }

    /** Snapshot captured before each propagation when interpolation is enabled. */
    public void previousComposedRenderOverlayInto(PartTransform dest) {
        dest.set(previousComposedRenderOverlayLocal);
    }

    public boolean hasPreviousComposedRenderOverlay() {
        return hasPreviousComposedRenderOverlay;
    }

    /**
     * Copies {@link #composedRenderOverlayInto} into the interpolation snapshot — invoke before {@link com.sirolf2009.necromancy.multipart.TransformHierarchy#tick}
     * when smoothing is enabled.
     */
    public void snapshotPreviousComposedRenderOverlayForInterpolation() {
        previousComposedRenderOverlayLocal.set(composedRenderOverlayLocal);
        hasPreviousComposedRenderOverlay = true;
    }

    /** Writes merged render overlay from {@link com.sirolf2009.necromancy.multipart.TransformHierarchy} (layers + editor). */
    public void setComposedRenderOverlayFromPipeline(PartTransform mergedLocalOverlay) {
        composedRenderOverlayLocal.set(mergedLocalOverlay);
    }

    public PartDamageState damageState() {
        return damageState;
    }

    /** @deprecated Prefer {@link #simulationLocalTransform()} */
    @Deprecated
    public Vec3 localTranslation() {
        return simulationLocal.translation();
    }

    /** @deprecated Prefer {@link #simulationLocalTransform()} */
    @Deprecated
    public void setLocalTranslation(Vec3 localTranslation) {
        simulationLocal.setTranslation(localTranslation);
    }

    /** @deprecated Prefer quaternion pose on {@link #simulationLocalTransform()} */
    @Deprecated
    public float localYawDegrees() {
        Quaternionf q = new Quaternionf();
        simulationLocal.rotationInto(q);
        return QuaternionOps.yawDegreesHorizontal(q);
    }

    /** @deprecated Sets yaw only (pitch/roll cleared). */
    @Deprecated
    public void setLocalYawDegrees(float localYawDegrees) {
        simulationLocal.setRotation(QuaternionOps.fromYawDegrees(localYawDegrees));
    }

    public @Nullable ResourceLocation parentId() {
        return parentId;
    }

    public void setParentId(@Nullable ResourceLocation parentId) {
        this.parentId = parentId;
    }

    public List<AttachmentPoint> attachmentPointsView() {
        return Collections.unmodifiableList(attachmentPoints);
    }

    public void clearAttachmentPoints() {
        attachmentPoints.clear();
    }

    public void addAttachmentPoint(AttachmentPoint point) {
        attachmentPoints.add(point);
    }

    public boolean attachedToParent() {
        return attachedToParent;
    }

    public void setAttachedToParent(boolean attachedToParent) {
        this.attachedToParent = attachedToParent;
    }

    public MultipartPartActivityState partActivityState() {
        return partActivityState;
    }

    public void setPartActivityState(MultipartPartActivityState partActivityState) {
        this.partActivityState = partActivityState == null ? MultipartPartActivityState.ACTIVE : partActivityState;
    }

    public int activityQuietTicks() {
        return activityQuietTicks;
    }

    public void incrementActivityQuietTicks() {
        activityQuietTicks++;
    }

    public void resetActivityQuietTicks() {
        activityQuietTicks = 0;
    }

    /** Conservative hull for union bounds / dormant aggregates — includes sleeping / static bones. */
    public boolean participatesInUnionBroadphaseHull() {
        return hitbox.collisionEnabled()
            && attachedToParent
            && !damageState.hasFlag(PartFunctionalFlag.NO_COLLISION);
    }

    /**
     * Fine spatial-hash emission — excludes sleeping/static bones as configured by {@link MultipartPartActivityState}.
     */
    public boolean contributesBroadphase() {
        return participatesInUnionBroadphaseHull()
            && partActivityState.contributesFineBroadphaseSlots();
    }

    /** When true, descendants are omitted from fine spatial hashing while this subtree sleeps. */
    public boolean collapsesSubtreeFineIndexing() {
        return partActivityState.collapsesSubtreeFineIndexing();
    }

    /**
     * @deprecated Prefer {@link #partActivityState()} — {@code true} maps to {@link MultipartPartActivityState#SLEEPING}.
     */
    @Deprecated
    public boolean broadphaseSleeping() {
        return partActivityState == MultipartPartActivityState.SLEEPING;
    }

    /**
     * @deprecated Prefer {@link #setPartActivityState(MultipartPartActivityState)}.
     */
    @Deprecated
    public void setBroadphaseSleeping(boolean broadphaseSleeping) {
        setPartActivityState(broadphaseSleeping ? MultipartPartActivityState.SLEEPING : MultipartPartActivityState.ACTIVE);
    }

    public boolean broadphaseSubtreeProxyOnly() {
        return broadphaseSubtreeProxyOnly;
    }

    public void setBroadphaseSubtreeProxyOnly(boolean broadphaseSubtreeProxyOnly) {
        this.broadphaseSubtreeProxyOnly = broadphaseSubtreeProxyOnly;
    }

    public WorldPose simulationWorldPose() {
        return simulationWorld;
    }

    public void setSimulationWorldPose(WorldPose simulationWorld) {
        this.simulationWorld = simulationWorld == null ? WorldPose.identity() : WorldPose.copyOf(simulationWorld);
    }

    public WorldPose renderWorldPose() {
        return renderWorld;
    }

    public void setRenderWorldPose(WorldPose renderWorld) {
        this.renderWorld = renderWorld == null ? WorldPose.identity() : WorldPose.copyOf(renderWorld);
    }

    /** Previous tick simulation pose for client interpolation (when enabled). */
    public @Nullable WorldPose previousSimulationWorldPose() {
        return previousSimulationWorld;
    }

    public void setPreviousSimulationWorldPose(@Nullable WorldPose previousSimulationWorld) {
        this.previousSimulationWorld = previousSimulationWorld == null ? null : WorldPose.copyOf(previousSimulationWorld);
    }

    public ResolvedObb simulationCollisionObb() {
        return simulationCollisionObb;
    }

    public void setSimulationCollisionObb(ResolvedObb simulationCollisionObb) {
        this.simulationCollisionObb = ResolvedObb.copyOf(simulationCollisionObb);
    }

    /** Broad-phase axis-aligned bound for cheap rejection (contains oriented simulation volume). */
    public AABB simulationBroadphase() {
        return simulationBroadphase;
    }

    public void setSimulationBroadphase(AABB simulationBroadphase) {
        this.simulationBroadphase = simulationBroadphase;
    }

    /** @deprecated Alias for {@link #simulationBroadphase()} used by legacy callers. */
    @Deprecated
    public WorldPose worldPose() {
        return simulationWorld;
    }

    /** @deprecated Use {@link #setSimulationWorldPose(WorldPose)} */
    @Deprecated
    public void setWorldPose(WorldPose worldPose) {
        setSimulationWorldPose(worldPose);
    }

    /** @deprecated Use {@link #simulationBroadphase()} */
    @Deprecated
    public AABB worldHitbox() {
        return simulationBroadphase;
    }

    /** @deprecated Use {@link #setSimulationBroadphase(AABB)} */
    @Deprecated
    public void setWorldHitbox(AABB worldHitbox) {
        setSimulationBroadphase(worldHitbox);
    }

    public BodyPartNode copySkeleton() {
        BodyPartNode n = new BodyPartNode(id, hitbox);
        n.simulationLocal.set(simulationLocal);
        n.renderOverlayLocal.set(renderOverlayLocal);
        n.parentId = parentId;
        n.attachedToParent = attachedToParent;
        for (AttachmentPoint ap : attachmentPoints) {
            n.addAttachmentPoint(new AttachmentPoint(ap.socketId(), ap.socketTransform(), ap.bindSpace(),
                ap.childPartId(), ap.priority()));
        }
        n.damageState.copyFrom(damageState);
        n.partActivityState = partActivityState;
        n.activityQuietTicks = activityQuietTicks;
        n.broadphaseSubtreeProxyOnly = broadphaseSubtreeProxyOnly;
        n.composedRenderOverlayLocal.setToIdentity();
        n.previousComposedRenderOverlayLocal.setToIdentity();
        n.hasPreviousComposedRenderOverlay = false;
        return n;
    }

    private static AABB deadHitbox() {
        return new AABB(0, 0, 0, 0, 0, 0);
    }

    /** Refresh oriented + broad-phase collision caches from current simulation pose (called by {@link com.sirolf2009.necromancy.multipart.TransformHierarchy}). */
    public void refreshSimulationCollision(WorldPose simPose) {
        if (!hitbox.collisionEnabled() || damageState.hasFlag(PartFunctionalFlag.NO_COLLISION)) {
            simulationCollisionObb = new ResolvedObb(Vec3.ZERO, QuaternionOps.fromYawDegrees(0f),
                new Vector3f(1e-4f, 1e-4f, 1e-4f));
            simulationBroadphase = deadHitbox();
            return;
        }
        var vol = hitbox.localOrientedVolume();
        simulationCollisionObb = CollisionResolve.resolve(simPose, vol);
        Quaternionf obbRot = new Quaternionf();
        simulationCollisionObb.orientationInto(obbRot);
        Vector3f obbHalf = new Vector3f();
        simulationCollisionObb.halfExtentsInto(obbHalf);
        simulationBroadphase = OrientedBounds.expandingAabb(simulationCollisionObb.centerWorld(), obbRot, obbHalf);
    }
}
