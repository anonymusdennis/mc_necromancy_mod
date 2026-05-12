# Necromancy Mob Framework (v0.5)

This document describes how the Necromancy 1.21.1 port assembles a minion
from five body-part adapters, how movement and sound are decided, and how
to extend the framework with new mobs and per-bodypart abilities.

The corresponding source packages are:

* `com.sirolf2009.necromancy.api`            -- adapter + profile contracts
* `com.sirolf2009.necromancy.api.feature`    -- `PartFeature` interface + registry
* `com.sirolf2009.necromancy.entity`         -- `EntityMinion`, `MinionAssembly`, AI
* `com.sirolf2009.necromancy.client.renderer` -- `MinionAssembler`
* `com.sirolf2009.necromancy.client.model`   -- `MinionPartCache`, `BorrowedGeometry`

```mermaid
flowchart TD
    Adapter[NecroEntityBase adapter] --> Geometry[BodyPart[] head/torso/arms/legs]
    Adapter --> Loco[locomotion()]
    Adapter --> Voice[voice()]
    Adapter --> Features[features(slot)]
    Geometry --> PartCache[MinionPartCache]
    Loco --> Resolver[MinionAssembly.resolve]
    Resolver --> EntityMinion
    EntityMinion --> AI[speed / step / swim / hop / static]
    EntityMinion --> Renderer[MinionAssembler]
    PartCache --> Renderer
    Renderer --> ScreenOutput[on-screen minion]
```

## 1. Adapter authoring checklist

To add a new vanilla mob (say `axolotl`):

1. Create `NecroEntityAxolotl extends NecroEntityBase` (or
   `NecroEntityBiped` / `NecroEntityQuadruped` if you want their default
   walk-cycle).
2. Override `initHead / initTorso / initArmLeft / initArmRight / initLegs`
   to define geometry.  Either:
   * type the cubes by hand using `new BodyPart(this, u, v).addBox(...)`, or
   * borrow vanilla geometry: `BorrowedGeometry.borrow(this,
     ModelLayers.AXOLOTL, "head")`.
3. Override `setAttributes(LivingEntity, BodyPartLocation)` so each slot
   contributes to the minion's stats.
4. Override `locomotion()` if the legs slot should walk faster, hop, or
   swim differently from the default zombie-walk:
   ```java
   @Override public LocomotionProfile locomotion() {
       return LocomotionProfile.walk(0.85F, SoundEvents.AXOLOTL_IDLE_AIR);
   }
   ```
5. Override `voice()` so the minion talks like an axolotl when this
   adapter is the torso (or head):
   ```java
   @Override public VoiceProfile voice() {
       return new VoiceProfile(SoundEvents.AXOLOTL_IDLE_AIR,
                               SoundEvents.AXOLOTL_HURT,
                               SoundEvents.AXOLOTL_DEATH, 1F, 1F);
   }
   ```
6. Optionally override `features(BodyPartLocation)` to attach extra
   abilities (e.g. an ender pearl teleport feature on the legs slot of
   an enderman).  See `SaddleFeature` for a complete worked example.
7. Register the adapter in `RegistryNecromancyEntities` (the only place
   the runtime sees adapter instances).

## 2. Locomotion profiles

`LocomotionProfile` is a record with fields:

| Field              | Meaning                                                           |
| ------------------ | ----------------------------------------------------------------- |
| `type`             | `WALK / HOP / SWIM / STATIC`                                      |
| `speedMultiplier`  | multiplied into MOVEMENT_SPEED via a transient AttributeModifier  |
| `stepSound`        | played on every `playStepSound` (null = silent)                   |
| `stepVolume`       | passed to `playSound`                                             |
| `stepPitch`        | passed to `playSound`                                             |
| `hopIntervalTicks` | only used for HOP -- ticks between jump impulses                  |
| `liftPixels`       | filled by the runtime during ARMS_AS_LEGS fallback                |

Standard factories live on `LocomotionProfile`:

* `WALK_DEFAULT`    -- 1.0 speed, zombie step sound
* `STATIC`          -- cannot move
* `SWIM_DEFAULT`    -- 0.6 speed, no step sound (squid)
* `walk(mul, step)` -- tuned WALK
* `hop(mul, ticks, step)` -- tuned HOP (rabbit etc.)
* `armsAsLegs(src)` -- transform a profile into the arms-as-legs form
  (halved speed, lift = 10 px)

## 3. Locomotion mode resolution

`MinionAssembly.resolve(EntityMinion)` (or
`MinionAssembly.fromAdapters(...)` for the altar preview) returns a
record with one of three modes:

1. **STANDING** -- legs slot is filled.  The active profile is
   `legs.locomotion()` directly.
2. **ARMS_AS_LEGS** -- no legs but a torso and at least one arm are
   present.  The active profile is
   `LocomotionProfile.armsAsLegs(armAdapter.locomotion())`, which:
   * collapses any source type to WALK,
   * halves the source `speedMultiplier`,
   * sets `liftPixels = 10` so the renderer lifts the model so the
     arm-tips touch the ground.
3. **STATIC** -- no legs, no torso (or no arms at all), or the torso
   adapter declared a STATIC profile in some hypothetical override.
   Velocity is clamped, navigation stopped.

`EntityMinion.tick()` calls `refreshAssembly()` every tick.  The expensive
operations (feature attach/detach, MOVEMENT_SPEED modifier rebuild,
SwimRandomGoal install/remove) only fire when the *structural* shape
changes (different adapter in any slot), which is cheap.

## 4. ARMS_AS_LEGS rendering

`MinionAssembler.renderArmsAsLegs` is a separate render path:

1. Lift the whole stack by `profile.liftPixels() / 16f` so the arm tips
   touch ground.
2. Render the left arm first, then the right arm, **at the lifted
   ground** (no torso translate).  The animation hint passed to
   `adapter.setAnim` is `BodyPartLocation.Legs`, so e.g. cow arms call
   the cow's quadruped leg walk-cycle on themselves.
3. Halved swing speed (`speed * 0.5f`) so the mob plods along.
4. Translate up to the arm's shoulder height (`firstArm.pose.y`).
5. Render the torso with `forceRestPose=true` -- no quadruped flat-lay,
   no head tracking on torso parts.
6. Render the head at the torso's `headPos`, also `forceRestPose=true`
   and `headYaw=0, headPitch=0`.

## 5. Voice profiles

`VoiceProfile` is a flat record (`ambient / hurt / death / volume /
pitch`).  The minion picks its voice from:

1. the TORSO adapter's `voice()`, else
2. the HEAD adapter's `voice()`, else
3. `VoiceProfile.ZOMBIE`.

`EntityMinion.getAmbientSound / getHurtSound / getDeathSound` simply read
the cached assembly's voice.

## 6. PartFeature framework

`PartFeature` is the per-bodypart ability hook.  Lifecycle:

```
adapter swap on slot S
    -> for each old feature on S: onDetach
    -> for each new feature on S: onAttach
every server tick
    -> for every attached feature: serverTick
player right-clicks the minion
    -> for every attached feature on every slot: onPlayerInteract
       (first non-PASS short-circuits)
minion lands a melee attack
    -> for every attached feature on every slot: onAttack
minion takes damage (after LivingEntity#hurt succeeds)
    -> for every attached feature on every slot: onHurt
JEI / tooltip rendering of a body-part item
    -> appendTooltip
```

The minion's renderer / movement code never inspects features directly;
features only see `EntityMinion + BodyPartLocation` and act on that
context, so they are fully orthogonal to the assembly code.

### `SaddleFeature` (worked example)

`com.sirolf2009.necromancy.api.feature.SaddleFeature` is a singleton
(`SaddleFeature.INSTANCE`).  It is auto-attached to the TORSO slot for
any adapter that implements `ISaddleAble` (cow, pig, spider, squid, camel) by
the default `NecroEntityBase.features(...)` implementation:

```java
public java.util.List<PartFeature> features(BodyPartLocation location) {
    if (location == BodyPartLocation.Torso && this instanceof ISaddleAble) {
        return java.util.List.of(SaddleFeature.INSTANCE);
    }
    return java.util.List.of();
}
```

Behaviour:

* right-click with a vanilla `Items.SADDLE` -> sets `isSaddled=true`,
  consumes the saddle in survival.
* right-click with `Items.SHEARS` while saddled -> drops the saddle and
  unsets `isSaddled`.
* right-click empty-handed while saddled and the player is the owner ->
  the player mounts the minion.

The saddle texture overlay itself is still drawn from
`MinionAssembler.renderGroup` when `isSaddled()` returns true; the
feature only handles the gameplay.

To author a new feature, mirror the saddle pattern:

```java
public final class FireBreathFeature implements PartFeature {
    public static final FireBreathFeature INSTANCE =
        FeatureRegistry.register(new FireBreathFeature());
    private FireBreathFeature() {}
    @Override public String id() { return "necromancy:fire_breath"; }
    @Override public void onAttack(EntityMinion m, BodyPartLocation slot, LivingEntity target) {
        if (slot != BodyPartLocation.Head) return;
        target.setRemainingFireTicks(40);
    }
}
```

Then attach from a Blaze adapter:

```java
@Override public List<PartFeature> features(BodyPartLocation loc) {
    return loc == BodyPartLocation.Head
        ? List.of(FireBreathFeature.INSTANCE)
        : super.features(loc);
}
```

## 7. Borrowing vanilla geometry

`BorrowedGeometry` lets adapter authors avoid retyping cuboids by hand
when a vanilla model already exists.  Example:

```java
@Override public BodyPart[] initHead() {
    return BorrowedGeometry.borrow(this, COW_LAYER, "head");
}

@Override public BodyPart[] initLegs() {
    return BorrowedGeometry.borrowAll(this, COW_LAYER, List.of(
        "right_hind_leg", "left_hind_leg",
        "right_front_leg", "left_front_leg"));
}
```

`COW_LAYER` is `CowModel.createBodyLayer()` (a `LayerDefinition`).

The reflective extractor walks the `MeshDefinition` of the layer and
copies cubes + UVs into our `BodyPart` representation.  If extraction
fails (e.g. on a hardened module-path), the helper logs a warning and
returns an empty array; the adapter should provide a hand-typed
fallback.

## 8. Smoke tests for v0.5

The following minion configurations should now behave as described:

| Configuration                              | Expected behaviour                                           |
| ------------------------------------------ | ------------------------------------------------------------ |
| Squid head + cow torso + creeper legs      | walks at creeper speed, plays grass step, no swim.           |
| Squid head + cow torso + squid legs        | static on land, swims in water.                              |
| Wolf head only                             | renders at world origin, makes wolf voice, cannot move.      |
| Cow torso + cow arms (no legs)             | ARMS_AS_LEGS: lifted, halved speed, plodding gait, cow voice.|
| Saddled cow torso + zombie legs            | accepts saddle, owner can mount, walks at cow speed.         |

## 9. Post-v0.5 adapters + JEI

Nine modern vanilla species now register body-part items and sewing
recipes through `BorrowedGeometry`: **Allay, Axolotl, Camel, Frog, Rabbit,
Sniffer, Warden, Blaze, Goat**.  Camel piggybacks on the legacy cow saddle
texture for the BER overlay until custom art exists.

Optional **JEI** (`compileOnly` + `runtimeOnly` on `jei-1.21.1-neoforge*`)
loads `NecromancyJeiPlugin`, which exposes:

* Every `SewingRecipe` in a 4×4 layout mirroring the in-game sewing GUI (replaceable background texture **`assets/necromancy/textures/gui/jei/sewing.png`**, **132×88** pixels).
* A **Necromancy Handbook** category (`necromancy:guide`) with one info recipe per mod item (all body parts use the shared `jei.necromancy.guide.body_part` template plus mob/slot translations). Background **`assets/necromancy/textures/gui/jei/guide.png`**, **220×136** pixels.
  Catalyst items so players find the tab quickly: **Necronomicon** and **Summoning Altar** block item.

Additionally **Patchouli** ships as an optional runtime dependency in Gradle:
`grim_codex_book.json` crafts the **Grim Codex** (`necromancy:grim_codex`). Book
contents live under `assets/.../patchouli_books/grim_codex/` with `book.json` in
`data/.../patchouli_books/grim_codex/`. Regenerate chapter stubs via
`python3 scripts/generate_grim_codex.py` after bulk edits.

Sample combat-flavour features ship alongside the saddle reference impl:

* `EnderHeadBlinkTeleportFeature` — attached automatically when an enderman head is worn.
* `SkeletonArmVolleyFeature` — attached when skeleton arms are present (either arm slot).
* `BlazeTorsoFireGuardFeature` — attached by `NecroEntityBlaze` on torso rods.

Wire additional perks via `PartFeature.onHurt` / `serverTick` / `onAttack` as needed.

## 10. Multipart `RootMobEntity` pattern (`EntityMinion`)

Gameplay mobs that own per-limb collision participate in `com.sirolf2009.necromancy.multipart`:

1. Implement `RootMobEntity` on a `LivingEntity` subclass — delegate `asMultipartRoot()` to `this` and expose a single owned `TransformHierarchy` instance.
2. Rebuild topology inside an existing structural gate (for minions: `EntityMinion.refreshAssembly()` after bodypart adapters swap) using `HierarchyEditBatch`:
   - `try (var b = hierarchy.beginEditBatch()) { hierarchy.clearStructureInBatch(); … register roots/children … }`
3. Each tick **after** vanilla motion updates the mob pose, invoke `multipartTick()` so simulation poses and broad-phase slots refresh (`multipartBroadphaseAutoPublish` should return true on the dedicated server).
4. Route incoming damage hits through `MultipartDamageRouter` when you want limb-aware strikes; optional `multipartUsesPerPartHealth()` mirrors pools via `MultipartHealthAggregate`.

Authoritative bodypart JSON lives under `config/necromancy/bodypartconfigs` (note ideas.md typo **necormancy** vs actual **`necromancy`** path from `BodyPartConfigManager.configDirectory()`).

Regression toggles live in `necromancy-common.toml`: `minions.minionLegacyCompositeCollision` swaps back to yaw-only `MinionCompositeCollision`, and `minions.minionMultipartPerPartHealth` enables experimental per-part HP routing.
