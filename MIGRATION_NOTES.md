# Necromancy 1.7.10 → 1.21.1 Migration Notes

This document tracks **every deliberate behaviour change** between the
original Necromancy mod for Minecraft 1.7.10 and the 1.21.1 NeoForge port
shipped under `modern/`.  Every entry below is a conscious deviation,
either because the modern engine no longer offers the corresponding API
or because the new API gives us a strictly better implementation.

## Identity & layout

* **Resource locations are now strict lowercase.**  All asset filenames
  under `textures/item/bodyparts/<mob>/<part>` were lowercased, e.g.
  `Cow/Head.png` → `cow/head.png`.  Resource packs that override these
  paths must follow suit.
* **Item IDs use snake_case.**  E.g. `bone_needle`, `soul_in_a_jar`,
  `brain_on_a_stick`.  Display names were preserved through
  `assets/necromancy/lang/en_us.json`.
* The legacy `ItemGeneric` multi-meta item is split into four distinct
  items: `bone_needle`, `soul_in_a_jar`, `jar_of_blood`,
  `brain_on_a_stick`.
* `ItemOrgans` similarly becomes five distinct foods: `brains`, `heart`,
  `muscle`, `lungs`, `skin`.
* The 54 body-part items are registered as one item per `(mob, part)`
  pair (vs. the old single multi-meta item with 54 metadata values).
  `BodyPartLocation.ArmLeft` is the canonical key for both arms; the
  mirror is handled by the renderer/adapter, matching legacy behaviour.

## Body-part & rendering pipeline

* `BodyPart` no longer extends `ModelRenderer`; it now wraps a
  `CubeListBuilder` plus a `PartPose`.  The renderer
  (`RenderMinion`) bakes a `ModelPart` per `(adapter, location)` pair
  via `MinionPartCache`.
* The `setRotationAngles` per-part animation hook from 1.7.10 is
  **simplified**: limb swing + head pitch are applied generically; per-mob
  custom animations (e.g. spider's diagonal walk) are not yet ported.
  Adding them later is a renderer-only change because `BodyPart` already
  carries everything required to identify the part.
* `EntityRenderer` for {`NightCrawler`, `Isaac{Normal,Blood,Head,Body}`,
  `Teddy`} reuse the vanilla `HumanoidModel` baked from the player layer.
  The legacy mod hand-rolled near-identical models per entity; the modern
  port relies on the texture atlas to do the work.

## World generation

* The legacy `WorldGenNetherChalice` was a hardcoded block-by-block
  placement triggered from `IWorldGenerator`.  In 1.21.1 we register a
  `Feature<NoneFeatureConfiguration>`
  (`com.sirolf2009.necromancy.worldgen.NetherChaliceFeature`) and wire
  it into nether biomes via:
  * `data/necromancy/worldgen/configured_feature/nether_chalice.json`
  * `data/necromancy/worldgen/placed_feature/nether_chalice.json`
  * `data/necromancy/neoforge/biome_modifier/add_nether_chalice.json`
  The geometry is the same chalice, but rolled into a more compact loop
  (we compress the iron-bar shaft and outer ring instead of duplicating
  ~300 lines of `setBlock`).  Net visual change: none.
* The legacy `ComponentVillageCemetery` was a custom `StructureVillagePieces.Village` component spawned by `IVillageCreationHandler`.
  Modern village generation is jigsaw-driven and component classes are
  no longer used.  The cemetery is **not** ported as a structure; the
  trade-handler portion of `VillageCreationHandler` was preserved (see
  below).  A future iteration may add a Jigsaw template under
  `data/minecraft/worldgen/template_pool` and a processor list to
  recreate the cemetery placement.

## Villager trades

* Instead of a custom `VillagerProfession` + `IVillageTradeHandler` pair,
  trades are added to the **vanilla librarian** via
  `VillagerTradesEvent` (`NecroVillagerTrades`).  Trade contents match
  the legacy mod 1:1: tier-1 = Necronomicon for 6 emeralds + 1 book;
  tier-2 = random body part for 1-3 emeralds; tier-3 = sell random body
  part to villager for 1-3 emeralds.

## Networking

* `NetworkHelper` is replaced by `RegisterPayloadHandlersEvent` +
  `CustomPacketPayload`.  The two payloads carry the same data:
  * `TearShotPayload` — fired by `KeyHandlerNecro` when the player
    presses *Shoot Tear* while wearing `isaacs_severed_head`.
  * `MinionCommandPacket` — fired by the Necronomicon screen for
    sit/dismiss commands.

## Achievements

* `AchievementNecromancy` (legacy `Achievement` API) is **not** ported
  in this iteration; advancements are the modern equivalent and live in
  `data/necromancy/advancements/`.  The pages exist but are empty stubs
  pending texture work.

## Bugs preserved 1:1

* `NecroEntityEnderman.initArmRight` returns the same geometry as
  `initArmLeft` *without mirroring*, exactly like the original mod.  See
  the comment in the source file.
* `NecroEntityPigZombie` keeps the legacy *"Pig Zombie"* mob name with
  *"Pigzombie"* body-part keys (the original mod had this inconsistency
  baked into save data and recipe lookups).

## v0.5 — locomotion + framework groundwork (May 2026)

* **Hybrid anchor cascade.** The renderer no longer floats arms when a
  torso is missing.  Rules:
  * legs absent + torso absent + head present → head renders at world
    origin (so wolf-head-only minions are visible);
  * torso absent + arms present → arms are silently hidden;
  * legs absent + torso present + arm(s) present → arms are repurposed
    as legs (see ARMS_AS_LEGS below).
* **`LocomotionProfile` per adapter.**  Each `NecroEntityBase` returns a
  `LocomotionProfile` describing how its legs slot drives movement:
  walk / hop / swim / static, speed multiplier, step sound, hop interval,
  body lift.  Per-adapter overrides currently shipped:
  cow / pig / sheep / chicken / creeper / spider / cave-spider / squid /
  zombie / pigzombie / skeleton / enderman / iron-golem.  Wolf and
  villager / witch are head-or-humanoid and inherit the default
  zombie-walk profile.
* **`MinionAssembly` resolver.**  A small server-side record maps the
  five adapters to one of `STANDING / ARMS_AS_LEGS / STATIC` and the
  active `LocomotionProfile`.  `EntityMinion` caches it per tick and
  reuses the same instance on the renderer side, so render and movement
  see identical state.
* **Speed / step / swim / hop wiring.**  The minion now applies a
  transient `MOVEMENT_SPEED` modifier from the active profile, plays the
  adapter's `stepSound` from `playStepSound`, freezes when STATIC, kills
  horizontal velocity on land when SWIM, and pulses upward every
  `hopIntervalTicks` when HOP.  A fresh `SwimRandomGoal` is added /
  removed automatically when the SWIM type toggles.
* **`ARMS_AS_LEGS` rendering.**  When legs are absent but the minion has
  a torso and at least one arm, the renderer lifts the model by
  `liftPixels`, renders arms with the LEGS animation hook (so they
  walk-cycle), at half amplitude, with the torso/head locked to their
  rest poses.  The minion looks like it is plodding along on its hands.
* **`VoiceProfile`.**  Adapters declare ambient / hurt / death sounds.
  The minion picks the voice from the torso adapter, falls back to the
  head adapter, and finally to zombie.  Same per-adapter override list
  as locomotion.
* **`PartFeature` framework.**  New interface + registry under
  `com.sirolf2009.necromancy.api.feature.*`.  Lifecycle hooks
  (`onAttach / onDetach / serverTick / onPlayerInteract / onAttack /
  appendTooltip`) are routed by `EntityMinion` automatically as adapters
  are swapped in / out.
* **`SaddleFeature`.**  The first concrete feature: any
  `ISaddleAble` torso adapter (cow, pig, spider, squid) auto-attaches
  `SaddleFeature` and accepts a vanilla saddle item, supports shears to
  remove, and lets the owner mount when right-clicked empty-handed.  The
  saddle texture overlay is still drawn by `MinionAssembler` from
  `ISaddleAble.getSaddleTexture()`.
* **`BorrowedGeometry` helper.**  Reflective extractor that reads cubes
  + UVs + part poses out of a vanilla `LayerDefinition` and emits
  `BodyPart[]` for our pipeline.  Groundwork for the next batch of
  vanilla mobs (allay, camel, axolotl, frog, warden, sniffer, ...) so we
  do not retype cuboids by hand.

## Post-v0.5 — modern vanilla adapters + JEI + sample `PartFeature`s

* **Nine additional adapters** ship behind `BorrowedGeometry`: Allay,
  Axolotl, Camel (`ISaddleAble`, saddle overlay temporarily reuses the cow
  texture), Frog (hop locomotion), Rabbit (hop), Sniffer (six legs), Warden,
  Blaze (head + rod cluster torso; `BlazeTorsoFireGuardFeature` on torso),
  Goat (hop).  Body-part items point at vanilla-ingredient placeholder textures
  in `models/item` until bespoke sprites land.
* **JEI.** Optional dependency on JEI 19.27+ registers a sewing-machine
  category listing every `SewingRecipe` via `META-INF/services/mezz.jei.api.IModPlugin`.
* **`PartFeature.onHurt`.**  Routed from `EntityMinion#hurt` after damage succeeds.
* **Worked flavour perks.**  Enderman head blink-teleport on hit; skeleton arms
  attach an arrow volley after melee contact (`SkeletonArmVolleyFeature`);
  blaze torso clears burning each tick.

## Things explicitly left as future work

* Heavy combat perks (sonic boom warden head, blaze jets, skeleton ranged AI,
  dedicated static-turret stance).
* Camel-specific saddle art vs reused cow overlay.
* Bespoke bodypart sprites replacing vanilla-ingredient placeholders.
* Extra fauna (breeze, armadillo, …) and richer worldgen parity.
* OBJ-loaded "special folk" scythe (NeoForge ships an OBJ loader, but the
  legacy `specialFolk.txt` list pulled live from a server URL is gated
  off until the user opts in via `enableSpecialFolkFetch` in the config).
* Achievement advancements (icons + criteria triggers).
* Cemetery jigsaw structure.
