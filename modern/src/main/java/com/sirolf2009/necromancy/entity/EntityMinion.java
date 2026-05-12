package com.sirolf2009.necromancy.entity;

import com.sirolf2009.necromancy.api.BodyPartLocation;
import com.sirolf2009.necromancy.api.LocomotionProfile;
import com.sirolf2009.necromancy.api.NecroEntityBase;
import com.sirolf2009.necromancy.api.NecroEntityRegistry;
import com.sirolf2009.necromancy.api.feature.PartFeature;
import com.sirolf2009.necromancy.NecromancyClientConfig;
import com.sirolf2009.necromancy.NecromancyConfig;
import com.sirolf2009.necromancy.bodypart.MinionCompositeCollision;
import com.sirolf2009.necromancy.bodypart.MinionSkeletonBinder;
import com.sirolf2009.necromancy.entity.ai.SwimRandomGoal;
import com.sirolf2009.necromancy.item.NecromancyItems;
import com.sirolf2009.necromancy.multipart.RootMobEntity;
import com.sirolf2009.necromancy.multipart.TransformHierarchy;
import com.sirolf2009.necromancy.multipart.damage.DamagePipeline;
import com.sirolf2009.necromancy.multipart.damage.MultipartDamageRouter;
import com.sirolf2009.necromancy.multipart.damage.MultipartHealthAggregate;
import com.sirolf2009.necromancy.multipart.part.BodyPartNode;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * EntityMinion -- the player's tamable necromantic servant.
 *
 * <p>The mob:
 * <ul>
 *     <li>extends {@link TamableAnimal} -- inheritance from {@code EntityTameable}</li>
 *     <li>tracks 5 {@link BodyPartLocation} -> mob name strings via
 *         {@link SynchedEntityData} so the renderer can rebuild the correct
 *         model server-to-client.</li>
 *     <li>caches a {@link MinionAssembly} per tick (or whenever the body-part
 *         data changes) and uses it to drive movement speed, step sounds,
 *         arms-as-legs / swim / static behaviours, and voice.</li>
 *     <li>owns a {@code Map<BodyPartLocation, List<PartFeature>>} that is
 *         rebuilt on every adapter swap, with attach / detach / tick
 *         lifecycle calls so per-bodypart features (saddle, ranged turret
 *         in future, ...) just plug in.</li>
 *     <li>implements {@link RootMobEntity}: owns an ephemeral {@link TransformHierarchy} rebuilt from bodypart slots whenever assembly changes.
 *         Nothing is persisted to NBT — worlds reload graphs from synched slot strings + local bodypart definitions (integrated single-player shares configs;
 *         pure multiplayer clients may see an empty graph until replication lands under todo F06).</li>
 *     <li>follows owner, hurts owner's targets, sits when ordered, and
 *         attacks nearby Monsters.</li>
 * </ul>
 */
public class EntityMinion extends TamableAnimal implements RootMobEntity {

    private static final EntityDataAccessor<String> DATA_HEAD       = SynchedEntityData.defineId(EntityMinion.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> DATA_TORSO      = SynchedEntityData.defineId(EntityMinion.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> DATA_ARM_LEFT   = SynchedEntityData.defineId(EntityMinion.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> DATA_ARM_RIGHT  = SynchedEntityData.defineId(EntityMinion.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> DATA_LEGS       = SynchedEntityData.defineId(EntityMinion.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> DATA_SADDLED   = SynchedEntityData.defineId(EntityMinion.class, EntityDataSerializers.BOOLEAN);

    /** Packed navigation destination block (0 = none); synced for slim client overlays (goggles). */
    private static final EntityDataAccessor<Long> DATA_AI_NAV_DEST =
        SynchedEntityData.defineId(EntityMinion.class, EntityDataSerializers.LONG);
    /** Combat target UUID when present; goggles draw a faint line toward it. */
    private static final EntityDataAccessor<Optional<UUID>> DATA_AI_ATTACK_TARGET =
        SynchedEntityData.defineId(EntityMinion.class, EntityDataSerializers.OPTIONAL_UUID);

    /**
     * Identifier for the per-assembly speed modifier we apply on top of the
     * minion's base MOVEMENT_SPEED attribute.  Re-applied (after removal)
     * every time the assembly changes.
     */
    private static final ResourceLocation SPEED_MOD_ID =
        ResourceLocation.fromNamespaceAndPath("necromancy", "assembly_speed");

    /** Cached assembly snapshot.  Recomputed when the body-part data changes. */
    private MinionAssembly assembly = MinionAssembly.empty();

    /** Live features attached to each body-part location.  Empty lists are valid. */
    private final EnumMap<BodyPartLocation, List<PartFeature>> features = new EnumMap<>(BodyPartLocation.class);

    /** Goal we add for SWIM minions; tracked so it can be removed on rebuild. */
    private @Nullable Goal swimWanderGoal;

    /** Server-side counter for HOP locomotion impulses. */
    private int hopCooldown = 0;

    /** Server-only bodypart hitboxes (five-slot altar layout). */
    private List<AABB> compositeCollisionBoxes = List.of();
    /** Number of LEG-flag bodypart nodes currently touching the ground (0 = airborne). */
    private int legsTouchingGroundProbe;

    private final TransformHierarchy multipartHierarchy = new TransformHierarchy();

    public EntityMinion(EntityType<? extends EntityMinion> type, Level level) {
        super(type, level);
        this.setTame(false, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0, true) {
            @Override
            public boolean canUse() {
                return EntityMinion.this.getAssembly().hasFunctionalBrain() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return EntityMinion.this.getAssembly().hasFunctionalBrain() && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(6, new net.minecraft.world.entity.ai.goal.FollowOwnerGoal(this, 1.0, 10F, 2F));
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Monster.class, true) {
            @Override
            public boolean canUse() {
                return EntityMinion.this.getAssembly().hasFunctionalBrain() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return EntityMinion.this.getAssembly().hasFunctionalBrain() && super.canContinueToUse();
            }
        });
    }

    public static AttributeSupplier.Builder createAttributes() {
        return TamableAnimal.createMobAttributes()
            .add(Attributes.MAX_HEALTH,    20)
            .add(Attributes.MOVEMENT_SPEED, 0.30)
            .add(Attributes.FOLLOW_RANGE,  16)
            .add(Attributes.ATTACK_DAMAGE,  2)
            .add(Attributes.KNOCKBACK_RESISTANCE, 0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_HEAD,      "");
        builder.define(DATA_TORSO,     "");
        builder.define(DATA_ARM_LEFT,  "");
        builder.define(DATA_ARM_RIGHT, "");
        builder.define(DATA_LEGS,      "");
        builder.define(DATA_SADDLED,   false);
        builder.define(DATA_AI_NAV_DEST, 0L);
        builder.define(DATA_AI_ATTACK_TARGET, Optional.empty());
    }

    public void setBodyPartName(BodyPartLocation loc, String name) {
        switch (loc) {
            case Head     -> entityData.set(DATA_HEAD, name);
            case Torso    -> entityData.set(DATA_TORSO, name);
            case ArmLeft  -> entityData.set(DATA_ARM_LEFT, name);
            case ArmRight -> entityData.set(DATA_ARM_RIGHT, name);
            case Legs     -> entityData.set(DATA_LEGS, name);
        }
    }

    public String getBodyPartName(BodyPartLocation loc) {
        return switch (loc) {
            case Head     -> entityData.get(DATA_HEAD);
            case Torso    -> entityData.get(DATA_TORSO);
            case ArmLeft  -> entityData.get(DATA_ARM_LEFT);
            case ArmRight -> entityData.get(DATA_ARM_RIGHT);
            case Legs     -> entityData.get(DATA_LEGS);
        };
    }

    public NecroEntityBase getBodyPart(BodyPartLocation loc) {
        String name = getBodyPartName(loc);
        return name.isEmpty() ? null : NecroEntityRegistry.get(name);
    }

    /** Live assembly snapshot.  Always non-null.  Updated by {@link #refreshAssembly()}. */
    public MinionAssembly getAssembly() { return assembly; }

    public List<AABB> getCompositeCollisionBoxes() {
        return compositeCollisionBoxes;
    }

    /** True when at least one configured LEG-flag bodypart touches blocks beneath its probe volume (server). */
    public boolean necromancyLegsConfiguredTouchGround() {
        return legsTouchingGroundProbe > 0;
    }

    /** Number of grounded leg-flagged nodes (server); 0 = fully airborne. */
    public int necromancyLegsGroundedCount() {
        return legsTouchingGroundProbe;
    }

    public boolean isSaddled() { return entityData.get(DATA_SADDLED); }
    public void setSaddled(boolean v) { entityData.set(DATA_SADDLED, v); }

    /** Client-only overlay helper — navigation destination from server PathNavigation. */
    public Optional<BlockPos> getSyncedNavDestination() {
        long packed = entityData.get(DATA_AI_NAV_DEST);
        return packed == 0L ? Optional.empty() : Optional.of(BlockPos.of(packed));
    }

    public Optional<UUID> getSyncedAttackTargetId() {
        return entityData.get(DATA_AI_ATTACK_TARGET);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("Head",     entityData.get(DATA_HEAD));
        tag.putString("Torso",    entityData.get(DATA_TORSO));
        tag.putString("ArmLeft",  entityData.get(DATA_ARM_LEFT));
        tag.putString("ArmRight", entityData.get(DATA_ARM_RIGHT));
        tag.putString("Legs",     entityData.get(DATA_LEGS));
        tag.putBoolean("Saddled", isSaddled());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(DATA_HEAD,      tag.getString("Head"));
        entityData.set(DATA_TORSO,     tag.getString("Torso"));
        entityData.set(DATA_ARM_LEFT,  tag.getString("ArmLeft"));
        entityData.set(DATA_ARM_RIGHT, tag.getString("ArmRight"));
        entityData.set(DATA_LEGS,      tag.getString("Legs"));
        setSaddled(tag.getBoolean("Saddled"));
        refreshAssembly();
    }

    // ------------------------------------------------------------------ --
    //                            ASSEMBLY LIFECYCLE
    // ------------------------------------------------------------------ --

    /**
     * Re-resolves {@link #assembly} from the current synched body-part names.
     * If the structural shape has changed the runtime then:
     * <ul>
     *     <li>detaches features from any slot whose adapter changed,</li>
     *     <li>attaches features for the new adapters,</li>
     *     <li>re-applies the speed modifier and (re)installs the SwimRandomGoal.</li>
     * </ul>
     * Idempotent: safe every tick — feature churn / multipart topology rebuild run only when the bodypart <strong>structure</strong> changes ({@link MinionAssembly#structurallySameAs} short-circuit).
     */
    public void refreshAssembly() {
        MinionAssembly fresh = MinionAssembly.resolve(this);
        if (fresh.structurallySameAs(assembly)) {
            // structure stable -> still update the cached profile reference
            assembly = fresh;
            return;
        }
        MinionAssembly old = assembly;
        assembly = fresh;

        if (level() == null) {
            return;
        }

        if (level().isClientSide) {
            syncMultipartSkeletonFromSlots();
            return;
        }

        // Detach features whose adapter changed.
        for (BodyPartLocation loc : BodyPartLocation.values()) {
            NecroEntityBase before = adapterFor(old, loc);
            NecroEntityBase after  = adapterFor(fresh, loc);
            if (before == after) continue;
            for (PartFeature f : features.getOrDefault(loc, List.of())) {
                f.onDetach(this, loc);
            }
            features.remove(loc);

            if (after != null) {
                List<PartFeature> attached = new ArrayList<>();
                for (PartFeature feat : after.features(loc)) {
                    feat.onAttach(this, loc);
                    attached.add(feat);
                }
                if (!attached.isEmpty()) features.put(loc, attached);
            }
        }

        applySpeedModifier(fresh.profile());
        rebuildLocomotionGoals(fresh);

        syncMultipartSkeletonFromSlots();
    }

    /**
     * Rebuilds (or clears) the multipart graph from synched bodypart slots — runs on both logical sides so debug overlays
     * can approximate poses on the integrated client. Server additionally seeds per-part HP when enabled.
     */
    private void syncMultipartSkeletonFromSlots() {
        if (useLegacyCollision()) {
            try (var batch = multipartHierarchy.beginEditBatch()) {
                multipartHierarchy.clearStructureInBatch();
            }
            return;
        }
        MinionSkeletonBinder.rebuild(this, multipartHierarchy);
        if (!level().isClientSide) {
            initMultipartPartHealthFromVanilla();
        }
    }

    private static NecroEntityBase adapterFor(MinionAssembly a, BodyPartLocation loc) {
        if (a == null) return null;
        return switch (loc) {
            case Head     -> a.head();
            case Torso    -> a.torso();
            case ArmLeft  -> a.armLeft();
            case ArmRight -> a.armRight();
            case Legs     -> a.legs();
        };
    }

    /** Recomputes the persistent MOVEMENT_SPEED modifier for the active profile. */
    private void applySpeedModifier(LocomotionProfile profile) {
        AttributeInstance speed = getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) return;
        speed.removeModifier(SPEED_MOD_ID);
        // multiplier: 1.0 means "leave base alone", 0.0 means "freeze in place".
        // Modeled as MULTIPLY_TOTAL with (mult - 1) so 1.0 -> 0 delta.
        double delta = profile.speedMultiplier() - 1.0;
        if (Math.abs(delta) > 1.0e-4 || profile.speedMultiplier() <= 0.0F) {
            speed.addTransientModifier(new AttributeModifier(
                SPEED_MOD_ID, delta, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }

    /** Adds / removes the SwimRandomGoal whenever the assembly switches in/out of SWIM. */
    private void rebuildLocomotionGoals(MinionAssembly a) {
        boolean wantSwim = a.profile().type() == LocomotionProfile.LocomotionType.SWIM;
        if (wantSwim && swimWanderGoal == null) {
            swimWanderGoal = new SwimRandomGoal(this, 1.0);
            this.goalSelector.addGoal(7, swimWanderGoal);
        } else if (!wantSwim && swimWanderGoal != null) {
            this.goalSelector.removeGoal(swimWanderGoal);
            swimWanderGoal = null;
        }
    }

    // ------------------------------------------------------------------ --
    //                              TICK / AI
    // ------------------------------------------------------------------ --

    @Override
    public void tick() {
        super.tick();
        // Re-resolve on both sides: client uses it for rendering / step sounds,
        // server uses it for AI and feature ticking.
        refreshAssembly();
        if (!level().isClientSide) {
            if (!useLegacyCollision() && !multipartHierarchy.nodes().isEmpty()) {
                multipartTick();
                compositeCollisionBoxes = multipartHierarchy.collectCollisionBoxes();
            } else {
                compositeCollisionBoxes = MinionCompositeCollision.buildWorldBoxes(this);
            }
            for (Map.Entry<BodyPartLocation, List<PartFeature>> e : features.entrySet()) {
                for (PartFeature f : e.getValue()) f.serverTick(this, e.getKey());
            }
        } else {
            if (!useLegacyCollision() && !multipartHierarchy.nodes().isEmpty()) {
                multipartTick();
            }
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide) return;

        LocomotionProfile profile = assembly.profile();
        legsTouchingGroundProbe = useLegacyCollision() || multipartHierarchy.nodes().isEmpty()
            ? MinionCompositeCollision.legsConfiguredTouchGround(this)
            : MinionCompositeCollision.legsHierarchyTouchGround(this, multipartHierarchy);
        switch (profile.type()) {
            case STATIC -> {
                // Hard-stop: zero out lateral velocity so a STATIC minion can't drift.
                Vec3 v = getDeltaMovement();
                setDeltaMovement(0, v.y, 0);
                getNavigation().stop();
            }
            case SWIM -> {
                // Stay put on land: kill horizontal velocity unless we're in water.
                if (!isInWater()) {
                    Vec3 v = getDeltaMovement();
                    setDeltaMovement(v.x * 0.4, v.y, v.z * 0.4);
                    getNavigation().stop();
                }
            }
            case HOP -> {
                if (onGround()) {
                    if (--hopCooldown <= 0) {
                        // small upward impulse + carry-forward
                        Vec3 v = getDeltaMovement();
                        setDeltaMovement(v.x, 0.42, v.z);
                        hopCooldown = Math.max(1, profile.hopIntervalTicks());
                        if (profile.stepSound() != null) {
                            playSound(profile.stepSound(), profile.stepVolume(), profile.stepPitch());
                        }
                    }
                }
            }
            case WALK -> {
                // nothing special; vanilla movement controller does the work
            }
        }
        publishAiTelemetry();
    }

    /** Low-rate sync for necromancer goggles — avoids enumerating full paths to stay cheap on bandwidth. */
    private void publishAiTelemetry() {
        if (tickCount % 8 != 0) return;
        PathNavigation nav = getNavigation();
        long packed = 0L;
        if (!nav.isDone()) {
            BlockPos tp = nav.getTargetPos();
            if (tp != null) {
                packed = tp.asLong();
            }
        }
        entityData.set(DATA_AI_NAV_DEST, packed);
        LivingEntity tgt = getTarget();
        entityData.set(DATA_AI_ATTACK_TARGET,
            tgt != null && tgt.isAlive() ? Optional.of(tgt.getUUID()) : Optional.empty());
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        SoundEvent step = assembly.profile().stepSound();
        if (step == null) {
            // Adapter explicitly silent -- skip the vanilla footstep entirely.
            return;
        }
        playSound(step, assembly.profile().stepVolume(), assembly.profile().stepPitch());
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        InteractionResult parent = super.mobInteract(player, hand);
        if (parent != InteractionResult.PASS) return parent;
        for (Map.Entry<BodyPartLocation, List<PartFeature>> e : features.entrySet()) {
            for (PartFeature f : e.getValue()) {
                InteractionResult r = f.onPlayerInteract(this, e.getKey(), player, hand);
                if (r != InteractionResult.PASS) return r;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        boolean ok = super.doHurtTarget(target);
        if (ok && target instanceof LivingEntity living) {
            for (Map.Entry<BodyPartLocation, List<PartFeature>> e : features.entrySet()) {
                for (PartFeature f : e.getValue()) f.onAttack(this, e.getKey(), living);
            }
        }
        return ok;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (level().isClientSide()) {
            return super.hurt(source, amount);
        }
        if (useLegacyCollision() || multipartHierarchy.nodes().isEmpty()) {
            boolean ok = super.hurt(source, amount);
            if (ok) notifyFeaturesHurt(source, amount);
            return ok;
        }
        BodyPartNode part = null;
        Vec3 srcPos = source.getSourcePosition();
        if (srcPos != null) {
            part = MultipartDamageRouter.findPartAtWorldPoint(multipartHierarchy, srcPos);
        }
        if (part == null) {
            var direct = source.getDirectEntity();
            if (direct != null) {
                part = MultipartDamageRouter.findPartAlongSegment(multipartHierarchy, direct.position(), position());
            }
        }
        if (part == null) {
            boolean ok = super.hurt(source, amount);
            if (ok) notifyFeaturesHurt(source, amount);
            return ok;
        }
        if (!multipartUsesPerPartHealth()) {
            float m = DamagePipeline.modifyIncoming(amount, part);
            boolean ok = super.hurt(source, m);
            if (ok) notifyFeaturesHurt(source, amount);
            return ok;
        }
        var result = MultipartDamageRouter.applyDamageToPart(this, part, source, amount);
        if (!result.applied()) {
            return false;
        }
        setHealth(MultipartHealthAggregate.totalCurrent(multipartHierarchy));
        notifyFeaturesHurt(source, amount);
        return true;
    }

    private void notifyFeaturesHurt(DamageSource source, float amount) {
        for (Map.Entry<BodyPartLocation, List<PartFeature>> e : features.entrySet()) {
            for (PartFeature f : e.getValue()) {
                f.onHurt(this, e.getKey(), source, amount);
            }
        }
    }

    @Override
    public boolean multipartPoseInterpolationCapture() {
        if (!level().isClientSide()) {
            return false;
        }
        return NecromancyClientConfig.MINION_MULTIPART_INTERPOLATION_CAPTURE.get()
            && !useLegacyCollision()
            && !multipartHierarchy.nodes().isEmpty();
    }

    /**
     * LAN replication backlog (plan F06): minion sockets stay ephemeral until persistent topology ids sync across clients.
     */
    @Override
    public void multipartConsumeTopologyNotify(int topologyRevision, long transformDirtyRevision, long propagationSerial) {
    }

    @Override
    public LivingEntity asMultipartRoot() {
        return this;
    }

    @Override
    public TransformHierarchy multipartHierarchy() {
        return multipartHierarchy;
    }

    @Override
    public boolean multipartBroadphaseAutoPublish() {
        return !level().isClientSide();
    }

    @Override
    public boolean multipartUsesPerPartHealth() {
        return NecromancyConfig.MINION_MULTIPART_PER_PART_HEALTH.get() && !useLegacyCollision();
    }

    @Override
    public float multipartResidualDamageToVanilla(float rawIncoming, float appliedToPart, BodyPartNode part, DamageSource source) {
        return 0f;
    }

    /** When true, use altar-era composite AABBs instead of {@link #multipartHierarchy} collision. */
    public boolean useLegacyCollision() {
        return NecromancyConfig.MINION_LEGACY_COMPOSITE_COLLISION.get();
    }

    @Override
    public void multipartCollectAnimationLayers(com.sirolf2009.necromancy.multipart.animation.MultipartAnimationFrame frame) {
        float walkPos   = walkAnimation.position(1.0f);
        float walkSpeed = walkAnimation.speed(1.0f);
        float headYaw   = yHeadRot - yBodyRot;
        float headPitch = getXRot();
        float attackAnim = getAttackAnim(1.0f);
        com.sirolf2009.necromancy.bodypart.MinionBodypartAnimator.collect(
            this, frame, walkPos, walkSpeed, headYaw, headPitch, attackAnim);
    }

    @Override
    public net.minecraft.world.phys.AABB makeBoundingBox() {
        if (!useLegacyCollision() && !multipartHierarchy.nodes().isEmpty()) {
            net.minecraft.world.phys.AABB union = multipartHierarchy.unionBroadphaseBounds();
            if (union.getXsize() > 1e-6 && union.getYsize() > 1e-6 && union.getZsize() > 1e-6) {
                return union;
            }
        }
        return super.makeBoundingBox();
    }

    private void initMultipartPartHealthFromVanilla() {
        if (!multipartUsesPerPartHealth()) return;
        var nodes = multipartHierarchy.nodes();
        if (nodes.isEmpty()) return;
        float share = getMaxHealth() / nodes.size();
        for (BodyPartNode n : nodes) {
            n.damageState().setMaxHealth(share);
            n.damageState().setCurrentHealth(share);
        }
    }

    // ------------------------------------------------------------------ --
    //                              MISC
    // ------------------------------------------------------------------ --

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return null; // minions cannot breed
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(NecromancyItems.BRAIN_ON_STICK.get());
    }

    @Override protected SoundEvent getAmbientSound() { return assembly.voice().ambient(); }
    @Override protected SoundEvent getHurtSound(DamageSource src) { return assembly.voice().hurt(); }
    @Override protected SoundEvent getDeathSound() { return assembly.voice().death(); }
}
