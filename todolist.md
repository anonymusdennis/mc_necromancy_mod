# Necromancy Mod — v1.0 Master Todo List

> **Vision recap (from problem statement):**  
> Tranquilize a zombie → scalpel it → harvest bodyparts → assemble a custom ragdoll-creature → summon it at the Operation Table (caged minion) or the Altar → creature is a *fully custom, physically-simulated entity* built from connected bodyparts with joints that rotate, a walking gait that emerges from the skeleton topology, and per-limb hitboxes.  
> The new minion system **completely replaces** the original 1.7.10 adapter-slot system for creature movement. The adapter framework stays only as a *bodypart library* (geometry + textures), not as the locomotion or collision driver.

---

## Key: What Is Already Done (Do Not Re-Implement)

- Multipart `TransformHierarchy` + `HierarchyEditBatch` ✅  
- `RootMobEntity` interface + integration in `EntityMinion` ✅  
- `BodyPartNode`, `AttachmentPoint`, `HitboxComponent`, `OrientedLocalVolume` ✅  
- Broadphase (spatial hash, activity governor, BVH queries) ✅  
- Multipart network replication pipeline (binary codecs, delta payloads) ✅  
- `MultipartDamageRouter` + `MultipartHealthAggregate` ✅  
- `BodyPartConfigManager` + `BodypartDefinitionJson` (JSON hitbox/attachment/flags/validation) ✅  
- `BlockBodypartDev` + `ScreenBodypartDev` (4-tab dev console, live preview, save-to-disk) ✅  
- `LocomotionProfile` + `MinionAssembly` resolver (STANDING / ARMS_AS_LEGS / STATIC) ✅  
- `VoiceProfile` per adapter ✅  
- `PartFeature` framework + `SaddleFeature`, `EnderHeadBlinkTeleportFeature`, `SkeletonArmVolleyFeature`, `BlazeTorsoFireGuardFeature` ✅  
- `BorrowedGeometry` vanilla model extractor ✅  
- Adapter library: Cow, Pig, Sheep, Chicken, Creeper, Spider, CaveSpider, Squid, Zombie, Pigzombie, Skeleton, Enderman, IronGolem, Wolf, Villager, Witch, Allay, Axolotl, Camel, Frog, Rabbit, Sniffer, Warden, Blaze, Goat ✅  
- `MinionAssembler` renderer (STANDING + ARMS_AS_LEGS render paths) ✅  
- `BlockAltar` + `BlockEntityAltar` + `ContainerAltar` + `ScreenAltar` + `AltarBlockEntityRenderer` (preview minion) ✅  
- `BlockSewing` + `SewingRecipe` + `CraftingManagerSewing` + `ScreenSewing` ✅  
- `BlockOperationTable` (block + block entity shell) ✅  
- JEI plugin (Sewing category + Guide category) ✅  
- Patchouli Grim Codex stubs ✅  
- Nether Chalice world gen feature ✅  
- Villager trades (`NecroVillagerTrades`) ✅  
- NecromancyConfig (TOML with all multipart flags) ✅  
- `MinionSkeletonBinder` (builds TransformHierarchy from altar slots) ✅  
- `CommandMinion` (sit/dismiss via Necronomicon) ✅  
- `CommandNecromancyDissectStub` (stub only — not functional) ⚠️ needs full implementation  
- `ItemMobCagePlaceholder` (creative-only shell, no behaviour) ⚠️ needs full implementation  

---

## Phase 1 — Anesthetics & Tranquilizer System (ideas.md lines 4–75)

> **Goal:** Full sedative brewing chain → blowgun + tranquilizer darts → blade oil → tranquilized state on mobs and players.

### 1.1 — Brewing Chain Items
- [ ] Register `ItemSedativePotion` (extends PotionItem or wraps a custom MobEffect) — slowness + reduced damage sensitivity + slight visual blur  
- [ ] Register `ItemCrudeAnesthetic` — adds blackscreen overlay effect on top of sedative effects  
- [ ] Register `ItemRefinedAnesthetic` — near-total immobilization + pain suppression; unlocks surgery mechanics on hit mob  
- [ ] Add custom `MobEffect` entries: `SEDATED`, `CRUDE_ANESTHETIC`, `REFINED_ANESTHETIC` (all with levels 0–255 as ideas.md specifies)  
- [ ] Add `BrewingRecipeEvent` handlers:  
  - Awkward Potion + poppy petals/poppy extract → Sedative Potion  
  - Sedative Potion + fermented spider eye + sugar → Crude Anesthetic  
  - Crude Anesthetic + ghast tear OR chorus fruit → Refined Anesthetic  
  - Each step requires the previous potion (not shortcuttable)  

### 1.2 — Blowdart & Blade Oil
- [ ] Register `ItemBlowDart` (generic, no effect; base form)  
- [ ] Register `ItemTranquilizerDart` (crafted: 1 Refined Anesthetic + 8 Blowdarts → 8 darts); inherits parent potion color dynamically  
- [ ] Implement **dynamic coloring** — darts and blade oil inherit the exact ARGB color of the source potion (use `PotionUtils` color extraction)  
- [ ] Implement **any-potion compatibility** — the crafting table conversion (potion → 16 blade-oil) and 1 potion + 8 darts must work for ANY valid potion, not only the anesthetic series  
- [ ] Register `ItemBladeOil` — offhand item; any sword attack while holding blade oil in offhand applies the oil's effect via `LivingAttackEvent`; inherits potion color  
- [ ] Add tooltips to all dart and blade-oil items describing the source effect  

### 1.3 — Blowgun Item
- [ ] Register `ItemBlowGun` — right-click fires the first `ItemTranquilizerDart` stack in inventory as a projectile entity  
- [ ] Register `EntityTranquilizerDart` — projectile, applies `REFINED_ANESTHETIC` on hit; inherits color of its dart item  

### 1.4 — Tranquilized Entity State (mobs)
- [ ] When a mob's current HP ≥ effect level threshold (configurable, default 10 HP): apply Dinnerbone name tag rotation effect (flip entity upside-down client side), disable AI completely, set a 20-minute coma timer (configurable)  
- [ ] When HP < threshold: apply only nausea + blur overlay + slowness + muffled sounds (partial dose path)  
- [ ] Timer extension: hitting a tranquilized mob with another dart resets timer to max (does NOT stack stacks beyond one), but only if the mob is already fully tranquilized  
- [ ] Server-side `EntityDataAccessor<Boolean>` `TRANQUILIZED` and `EntityDataAccessor<Long>` `TRANQUILIZE_EXPIRE_TICK` synced to clients  
- [ ] On wake-up: re-enable AI, restore upright pose, remove effect  

### 1.5 — Tranquilized Player State
- [ ] When a player is hit: apply 1-minute coma (configurable)  
- [ ] Force player into spectator-like out-of-body perspective: spectate the shooter entity  
- [ ] If self-hit: camera orbits slowly above player's body position  
- [ ] Disable all player inputs while tranquilized  
- [ ] Muffle all sounds (volume → near-zero) during tranquilization  
- [ ] Shift right-click (number of stuck darts + 1 times) by another player or the afflicted player on themselves to revive early  
- [ ] Below-threshold dose on players: nausea + blur screen overlay + slowness + sound muffle (no full coma)  

### 1.6 — Config Entries (add to NecromancyConfig)
- [ ] `anesthetics.tranquilizeHpThreshold` — mob HP ≤ this value → partial dose only (default 10)  
- [ ] `anesthetics.mobComaDurationTicks` — default 24000 (20 minutes)  
- [ ] `anesthetics.playerComaDurationTicks` — default 1200 (1 minute)  

### 1.7 — Lang / Assets
- [ ] Lang keys for all new items, effects, tooltips  
- [ ] Placeholder textures (flat colored) for darts + blowgun + blade oil; note them as placeholder in MIGRATION_NOTES.md  
- [ ] Particle: small colored puff on dart hit (re-use `NecroFXParticle` or add a new particle type)  

---

## Phase 2 — Cage System (ideas.md lines 4–6, 77)

> **Goal:** A real cage item that can capture any tranquilized vanilla-friendly mob or any minion, storing it as NBT, suspending time while caged, releasing it anesthetized.

### 2.1 — Cage Item
- [ ] Replace `ItemMobCagePlaceholder` with a real `ItemMobCage`; remove the placeholder registration  
- [ ] Crafting recipe for the cage (iron bars + iron ingots — choose a fitting shape)  
- [ ] Right-click on a **tranquilized** mob (any vanilla friendly mob + any `EntityMinion`): capture it  
  - Store entire entity NBT (including custom data, equipment, tame status, name) in the cage `ItemStack` compound tag  
  - Remove the entity from the world  
  - Cage item visually changes (renderer shows a mob inside — or use an item model with a `CustomModelData` predicate for "filled")  
- [ ] Right-click cage (filled) on the ground: release the mob  
  - Spawn entity from stored NBT at target block  
  - Re-apply tranquilized state (if the mob was tranquilized when captured, release it still tranquilized with remaining timer)  
  - Clear cage tag → cage becomes empty again  
- [ ] No time passes while caged (entity ticks stop — achieved by simply not ticking NBT in item form)  
- [ ] Cage cannot capture hostile non-minion mobs (add a whitelist gate: passive mobs + `EntityMinion`)  
- [ ] Lang keys + placeholder texture for filled/empty cage states  

---

## Phase 3 — Scalpel & Dissection (ideas.md lines 78–93)

> **Goal:** Scalpel item that dissects a tranquilized mob into bodyparts + organs; operates on minions with a kill-switch prompt.

### 3.1 — Scalpel Item
- [ ] Register `ItemScalpel` — iron + diamond crafting recipe; inherits sword enchantability (use a custom `Tier`)  
- [ ] Remove `CommandNecromancyDissectStub` once the real system is wired; or convert it to an admin helper  

### 3.2 — Dissection of Vanilla Mobs
- [ ] `ItemScalpel` right-click interaction on a **tranquilized** mob:  
  - Map the mob's entity type to the available adapter set to determine which body-part items it can drop  
  - Roll 1–3 bodypart drops; never exceed the mob's actual anatomy (cow: max 2 arms, 2 legs, 1 torso, 1 head); max Looting III multiplier  
  - Roll 1–3 organ drops (Brains, Heart, Muscle, Lungs, Skin); prevent duplicating unique organs (max 1 brain per mob)  
  - Apply looting enchantment from held scalpel to all rolls  
  - Kill the mob after dissection (or reduce to 1 HP if dissection is aborted)  
- [ ] Spawn dropped items at mob's position  
- [ ] Register dissection loot tables (or implement procedurally via `ItemScalpel` use logic) for each mob type mapped to adapters  

### 3.3 — Dissection of Minions (kill-switch)
- [ ] When scalpel is used on an `EntityMinion` owned by the player, open a confirmation screen or send a chat prompt: "Activate biological kill-switch? [Yes / No]"  
- [ ] Yes → drop ALL bodyparts from every filled slot + any organ items; kill the minion  
- [ ] No → deal normal scalpel damage (never kill; always keep 1 HP)  
- [ ] Non-owner players cannot trigger the kill-switch prompt (only deal damage)  

### 3.4 — Dissection Table (future dissection of bodyparts into sub-components)
- [ ] Register `BlockDissectionTable` + `BlockEntityDissectionTable` (shell block entity)  
- [ ] Register `ContainerDissectionTable` + `ScreenDissectionTable` (placeholder GUI — one input slot, output slots)  
- [ ] Add crafting recipe for the dissection table block  
- [ ] Implement: inserting a bodypart item → produces muscles, bones, skin fragments (item-level dissection recipes); define as a `DissectionRecipe` type similar to `SewingRecipe`  
- [ ] Lang keys + block model + block texture (placeholder)  
- [ ] Grim Codex entry for dissection table  

---

## Phase 4 — Ragdoll-Based Minion Assembly System (ideas.md idea 2, problem statement)

> **Goal:** Replace the fixed 5-slot altar grid with a freeform 3×3 attachment-grid UI. Any bodypart connects to any other via attachment points. The resulting skeleton topology drives walking. This is the core new minion system.

### 4.1 — Altar UI Rework (3×3 attachment grid)
- [ ] Replace `ContainerAltar` fixed 7-slot layout with a freeform 3×3 grid of draggable bodypart slots  
  - `AltarAttachmentGridMock` already models the data shape — promote it to a real `AltarAttachmentGrid` block entity component  
- [ ] When a bodypart is inserted into any grid cell: find the nearest occupied cell's attachment point and auto-connect; visualize this with a line or arrow in the GUI  
- [ ] Connected bodyparts: grey out their cell (slot locked); only "endpoint" bodyparts (those with only one connection) remain un-greyed and removable  
- [ ] Right-click a free (unconnected) bodypart in a cell: cycle through its rotation variants if the adapter defines them (e.g., cow torso horizontal vs vertical)  
- [ ] Re-inserting a bodypart into the same cell: cycles through alternative nearest connection-point candidates (allows funky constructs)  
- [ ] Live preview minion in the altar GUI (reuse `AltarBlockEntityRenderer` preview — update it to read from the new grid topology instead of the 5 fixed slots)  
- [ ] No-head rule: creature is braindead and static if no bodypart flagged `HEAD` is present  
- [ ] No-movement rule: creature is static if no bodypart with the `LEG` or `ARM` flag touching the ground is present  
- [ ] Server → client sync of grid state via a new `AltarGridSyncPayload` extending the existing network stack  

### 4.2 — Ragdoll Skeleton Construction from Altar Grid
- [ ] Replace `MinionSkeletonBinder`'s star-topology approach with a proper **tree walk** over the altar grid connections  
  - Start from whichever bodypart is the "lowest" in the grid (feet/ground contact), walk up the connection tree  
  - Each connection edge becomes a parent–child link in `TransformHierarchy`; the `AttachmentPoint` socket drives relative pose  
- [ ] Validate that the topology forms a proper tree (no orphan islands, no loops) before spawning; report unconfigured bodyparts to player  
- [ ] Write topology to `EntityMinion` via new synced data accessor (`DATA_TOPOLOGY` — a compact list of (partId, parentPartId, socketIndex) triples) instead of the current 5 string slots  
- [ ] `MinionSkeletonBinder.rebuild()` reads the new topology data to reconstruct the `TransformHierarchy` on client after replication  

### 4.3 — Static Walk Animation Driver
- [ ] Implement a per-tick `StaticGaitDriver` that pushes `WeightedPartTransform` entries into `MultipartAnimationFrame` for all limb nodes in the hierarchy  
  - For each node flagged `LEG`: apply a sinusoidal rotation around the node's pivot axis (from `AttachmentPoint` config) keyed to `limbSwing` / `walkAnimationSpeed`  
  - For nodes flagged `ARM` used as legs (ARMS_AS_LEGS fallback): same driver, half amplitude  
  - For `HEAD`: apply yaw (horizontal look) and pitch (vertical look) from the entity's look direction  
  - `TORSO` nodes: locked to rest pose (no swing)  
- [ ] Wire `StaticGaitDriver` into `EntityMinion.tick()` after `multipartTick()`; pass its `MultipartAnimationFrame` to `MinionAssembler`  
- [ ] `MinionAssembler.renderAssembled()` must consume the `MultipartAnimationFrame` (add parameter) and apply weighted overlay transforms on top of simulation poses before rendering each `ModelPart`  
- [ ] Confirm the existing ARMS_AS_LEGS render path in `MinionAssembler` uses the new frame-based animation instead of the ad-hoc half-amplitude hack  

### 4.4 — Ground Contact Detection
- [ ] Add a per-bodypart `isGroundContact()` query in `TransformHierarchy`/`BodyPartNode` — true when the node's world AABB (from collision OBB) overlaps a solid block below it  
- [ ] `MinionAssembly.resolve()` consults ground contact: only bodyparts whose world-space bottom edge is ≤ 0.05 blocks above ground count as "movement providers"  
- [ ] If no flagged-LEG node is ground-touching but flagged-ARM nodes are: activate ARMS_AS_LEGS mode  
- [ ] Expose `isLegTouchingGround(partId)` as a debug overlay in `MultipartActivityDebugRenderer`  

### 4.5 — Operation Table — Full Implementation
- [ ] `ContainerOperationTable`: replace the 9 ghost placeholder slots with real functional slots  
  - Slot 0: caged minion input (accepts a filled `ItemMobCage` containing an `EntityMinion`)  
  - Slots 1–5: bodypart slots for replacement/addition  
  - Slot 6: tool slot (scalpel for removal, item determining the type of graft)  
  - Slot 7: output slot (removed bodypart drops here)  
- [ ] `BlockEntityOperationTable`: implement `serverTick()` logic  
  - When a caged minion + a target bodypart are both present: perform the graft operation (replace or add the bodypart on the minion stored in the cage)  
  - Operations require anesthetics (consume one `ItemRefinedAnesthetic` per operation)  
  - The minion in the cage remains alive; its NBT is mutated in place; re-cage on output  
- [ ] `ScreenOperationTable`: render a proper GUI  
  - Show a live preview of the minion (reuse `MinionAssembler`) with the proposed graft highlighted  
  - Buttons: "Graft", "Remove limb", "Cancel"  
  - Requires anesthetics counter / indicator  
- [ ] Grim Codex entry for operation table  
- [ ] JEI category for operation table grafts  

---

## Phase 5 — Developer Block — Full GUI Implementation (ideas.md lines 141–180)

> Most of the dev block is already implemented (`ScreenBodypartDev`, 4-tab UI, live preview, save-to-disk). The following sub-features are still stubs or missing.

### 5.1 — Live Preview Entity
- [ ] `EntityBodypartPreview` already registered — confirm it renders the current draft's hitbox (OBB wire-frame) and connection-point markers via `BodypartPreviewRenderer`; if broken, fix the render path  
- [ ] Implement hide/show toggles for:  
  - Mesh (texture/model visible or invisible)  
  - Collision outline (OBB wire)  
  - Socket crosses (attachment point position markers)  
  - Pivot crosses (rotation axis markers)  
  - All checkboxes already exist in `ScreenBodypartDev.tabPreview` — wire them to `BodypartPreviewMask` and propagate via `BodypartDevApplyPayload`  
- [ ] Rotation axis visualization: for `ARM` and `LEG` flagged parts, draw a colored axis line through the pivot point defined in the socket config  
- [ ] HEAD parts: render 3 rotation axes (yaw, pitch, roll) at the socket pivot  

### 5.2 — Part Selector in Dev Block
- [ ] Add a scrollable list / dropdown at the top of `ScreenBodypartDev` showing all registered bodypart item IDs  
- [ ] Selecting a part: load its current on-disk JSON into the draft; open the same tab state as before closing (retain GUI state across part switches)  
- [ ] If no config file exists for the selected part: init a blank draft stub (same as `BodyPartConfigManager.defaultStub()`)  
- [ ] Closing the GUI auto-saves all in-progress edits to the draft (not to disk — disk save is explicit via the "Save to disk" button)  

### 5.3 — Attachment Point Editor (Sockets Tab)
- [ ] The Sockets tab (`tabSockets`) already has position/euler/quat/pivot fields — confirm all fields write correctly into `BodypartDefinitionJson.attachments()` on Apply  
- [ ] Add "Add socket" button → appends a new default `BodypartAttachmentJson` to the list and selects it  
- [ ] Add "Remove socket" button → removes the currently selected socket  
- [ ] Previous/Next navigation buttons for multi-socket bodyparts  
- [ ] Verify the live preview entity updates socket cross positions immediately after Apply  

---

## Phase 6 — Bodypart Config Validation Gate — Finish Wiring (ideas.md lines 146–148)

- [ ] `BodyPartConfigGate.isUsable(itemStack)` must refuse: (a) no JSON file on disk, (b) `validated=false`; confirm this blocks ALL uses: altar slot inserts, operation table grafts, sewing machine recipe output, all  
- [ ] Client-side tooltip on unconfigured/unvalidated bodypart items: already has `item.necromancy.bodypart.unconfigured` and `not_validated_tooltip` lang keys — verify `ItemBodyPart.appendHoverText` calls `BodyPartConfigGate` correctly  
- [ ] `BlockEntityAltar.spawn()` must refuse (and send `message.necromancy.altar.parts_unconfigured` with the list of blocked part names) if any inserted slot holds an unvalidated part  
- [ ] Operation table must also gate on validation before processing a graft  
- [ ] Pre-ship: author and save validated JSON configs for all built-in bodyparts (all 25+ adapters × their parts); these ship in `config/necromancy/bodypartconfigs/` bundled with the mod jar via the default resource pack mechanism  

---

## Phase 7 — Walking Animation — Static Gait (problem statement explicit requirement)

> "The walking animation is static first please" — a pre-baked, looping walk cycle (no inverse kinematics), driven by `limbSwing`.

- [ ] Define a `StaticGaitAnimator` that for a given `LocomotionProfile.type`:  
  - `WALK`: sinusoidal leg swing ±35° around the socket pivot, 90° phase offset between left/right leg pairs  
  - `HOP`: both legs flex together on hop impulse frame; squash-and-stretch on `hopIntervalTicks`  
  - `SWIM`: tentacle/fin oscillation (octopus-style cosine fan) — already partially in `NecroEntitySquid.setAnim`  
  - `STATIC`: no animation contribution  
- [ ] Apply head yaw/pitch (from entity look direction) to all `HEAD` flagged nodes  
- [ ] Apply body-lean forward proportional to speed (offset `TORSO` pitch slightly forward at max speed)  
- [ ] Push all contributions via `MultipartAnimationFrame` so they layer correctly over simulation poses  
- [ ] Confirm adapter-specific overrides in `NecroEntityBase.setAnim()` still run *after* the static gait (so per-mob tweaks override generic gait where needed)  

---

## Phase 8 — Interconnection & Integration Pass

- [ ] **Altar → ragdoll topology → EntityMinion**: full round-trip: insert parts in 3×3 grid → confirm → spawn `EntityMinion` with correct `TransformHierarchy` and walking behavior  
- [ ] **Operation Table → minion modification**: cage a minion, graft a new part, release cage, confirm the minion's hierarchy rebuilt correctly  
- [ ] **Scalpel → dissection → bodyparts → altar/operation table**: dissect zombie, pick up bodyparts, assemble in altar, summon, fight it  
- [ ] **Cage → operation table → re-release**: cage a minion mid-combat, modify it, release it  
- [ ] **Bodypart dev block → altar**: configure a new bodypart in the dev block, save to disk, insert in altar, spawn, confirm hitbox and walk cycle use the new config  
- [ ] **Tranquilize → cage → release at operation table**: blowgun zombie → cage while tranquilized → release at operation table → scalpel for parts  
- [ ] All `PartFeature` hooks still fire correctly on the new ragdoll entity (saddle, blaze fire guard, ender teleport, skeleton volley)  

---

## Phase 9 — Art, Audio, and UX Polish

### 9.1 — Placeholder Textures (note all as explicit placeholders in MIGRATION_NOTES.md)
- [ ] Tranquilizer dart / blade oil — flat colored quads tinted by potion color  
- [ ] Blowgun — stretched bone texture placeholder  
- [ ] Scalpel — diamond-blade iron-handle placeholder  
- [ ] Cage (empty + filled) — iron bars sprite placeholder  
- [ ] Dissection table block — placeholder stone top + iron-bar sides  
- [ ] Operation table block — currently uses a plain box; mark as placeholder  
- [ ] Camel-specific saddle overlay art (currently reuses cow texture — noted in MIGRATION_NOTES.md, needs own art)  
- [ ] All post-v0.5 bodypart item sprites (Allay, Axolotl, Camel, Frog, Rabbit, Sniffer, Warden, Blaze, Goat) — currently vanilla-ingredient placeholders  

### 9.2 — Sounds
- [ ] Blowgun fire sound (whoosh)  
- [ ] Dart hit sound (thunk / wet hit)  
- [ ] Cage capture / release sounds  
- [ ] Scalpel cut sound (wet slice)  
- [ ] Operation table graft sounds  
- [ ] Tranquilized player heartbeat loop (muffled) — plays during coma  
- [ ] All new sound events registered in `NecromancySounds` (create this class if it does not exist) and referenced in `sounds.json`  

### 9.3 — Grim Codex (Patchouli) Content
- [ ] Chapter: "Sedatives & Anesthetics" — brewing chain, blowgun use, blade oil  
- [ ] Chapter: "The Cage" — capture mechanics, operation table use  
- [ ] Chapter: "Dissection" — scalpel use on mobs and minions  
- [ ] Chapter: "The 3×3 Altar" — how to assemble bodyparts, locking, rotation variants  
- [ ] Chapter: "Operation Table" — grafting, anesthetic requirement  
- [ ] Chapter: "Ragdoll Creatures" — how the ragdoll system works, walking gait, static animation  
- [ ] Fill in all existing stub entries that still say "TODO" or have empty `"text"` arrays  

### 9.4 — JEI
- [ ] Add JEI category for Dissection Table recipes once the recipe type exists  
- [ ] Add JEI category for Operation Table graft operations  
- [ ] `sewing.png` and `guide.png` textures (noted as missing in MOB_FRAMEWORK.md) — create placeholders  

---

## Phase 10 — Advancements

> Currently empty stubs in `data/necromancy/advancements/`.

- [ ] Create advancement JSON files (with placeholder icons where needed):  
  - `first_brew` — craft a Sedative Potion  
  - `tranquilizer` — hit a mob with a tranquilizer dart  
  - `first_dissection` — dissect a mob with the scalpel  
  - `caged` — cage a living mob  
  - `surgeon` — perform a graft at the operation table  
  - `first_ragdoll` — summon a ragdoll minion at the altar  
  - `kill_switch` — activate the biological kill-switch on a minion  
  - `chimera` — build a minion from 4 different mob species  

---

## Phase 11 — World Generation

- [ ] **Cemetery Jigsaw Structure** (noted in MIGRATION_NOTES.md as not ported):  
  - Create a Jigsaw template NBT under `data/minecraft/worldgen/template_pool/necromancy/cemetery/`  
  - Create a processor list JSON for variant block replacements  
  - Register as a jigsaw pool piece that can attach to village paths  
- [ ] **Nether Chalice** — already implemented; verify it spawns correctly in 1.21.1 nether biomes; no work needed unless broken  

---

## Phase 12 — Special Entities & Remaining Combat Perks

### 12.1 — Left-for-Later Combat Perks (MIGRATION_NOTES.md explicit list)
- [ ] **Warden head sonic boom** (`WardenHeadSonicBoomFeature`) — on attack, fire a sonic boom toward the target (use vanilla `SonicBoom` damage source)  
- [ ] **Blaze torso jets** (`BlazeTorsoJetFeature`) — periodic upward velocity boost while the minion is in mid-air; produces fire particle trail  
- [ ] **Skeleton ranged AI** (`SkeletonRangedTurretGoal`) — when minion has skeleton arm(s), it gains a ranged attack goal; the existing `SkeletonArmVolleyFeature` fires arrows on melee contact, the turret goal should fire from range when no melee contact; make it configurable (hold position vs chase)  
- [ ] **Dedicated static-turret stance** — minion with no leg parts and a skeleton arm(s) freezes in place and enters pure ranged mode  

### 12.2 — OBJ Special Scythe
- [ ] Implement `enableSpecialFolkFetch` config opt-in: fetch `specialFolk.txt` from the legacy URL  
- [ ] Use NeoForge OBJ loader to load `specialFolk.obj` model for the scythe item renderer (`ScytheItemRenderer`) for players on the whitelist  
- [ ] Gate entirely behind `NecromancyConfig.ENABLE_SPECIAL_FOLK_FETCH` (default off)  

---

## Phase 13 — Bug Fixes & Code TODOs

- [ ] `EntityMinion`: multiplayer clients see empty `TransformHierarchy` until replication lands (noted as "todo F06" in class Javadoc) — wire `MultipartReplicationBridge` to push topology on entity track  
- [ ] `ContainerOperationTable`: the comment "plan F03" describes a byte ledger for graft rollback/replay — implement this once real slot logic is in place  
- [ ] `BodypartPreviewGeom` / `BodypartPreviewDraftResolution` — confirm that preview entity spawning in the dev block is triggered correctly and despawns on GUI close (no orphaned preview entities)  
- [ ] `MinionCompositeCollision` (legacy AABB fallback) — keep but confirm it is fully superseded by the multipart system and toggleable via config; add a regression test or smoke configuration note  
- [ ] `AchievementNecromancy` not ported — covered in Phase 10 above; remove the reference from MIGRATION_NOTES.md once advancements ship  
- [ ] `NecroEntityEnderman.initArmRight` geometry bug (preserved from original mod, noted in MIGRATION_NOTES) — add a config flag `fixEndermanMirrorBug` (default false) so new players get the corrected geometry while legacy saves are unbroken  
- [ ] `NecroEntityPigZombie` mob name inconsistency ("Pig Zombie" vs "Pigzombie") — document clearly in MIGRATION_NOTES.md; do not fix silently (save-breaking); add a data migration command `/necromancy migrate pigzombie` for players who opt in  
- [ ] LAN multipart replication (noted in RELEASE_CHECKLIST_MULTIPART.md as follow-up) — persistent node IDs must survive world save/load for replication to be stable; implement stable `BodyPartNode` ID serialisation keyed to the topology slot  

---

## Phase 14 — Release Checklist Gates (from RELEASE_CHECKLIST_MULTIPART.md)

- [ ] All bodypart JSON configs ship with `"validated": true` in the mod jar  
- [ ] `multipart.enableMultipartTelemetry` defaults to `false` in shipped TOML  
- [ ] Idle minions rebuild `TransformHierarchy` only on assembly fingerprint change (not every tick) — verify via telemetry  
- [ ] Debug overlays (multipart hitboxes) align with actual collision when `minionLegacyCompositeCollision=false`  
- [ ] RC-F8 archive: short client + server log bundle with altar refusal list, idle topology revision steady, optional F3 multipart overlay sanity check  
- [ ] Build and test with JEI present and absent (optional dep)  
- [ ] Build and test with Patchouli present and absent (optional dep)  

---

## Summary — Work Remaining by Priority

| # | Area | Status | Effort |
|---|------|--------|--------|
| 1 | Anesthetics brewing chain + effects | ❌ Not started | Large |
| 2 | Blowgun + tranquilizer darts + blade oil | ❌ Not started | Medium |
| 3 | Tranquilized entity state (mob + player) | ❌ Not started | Large |
| 4 | Cage item (full implementation) | ⚠️ Placeholder only | Medium |
| 5 | Scalpel + mob dissection | ⚠️ Stub only | Large |
| 6 | Minion kill-switch via scalpel | ❌ Not started | Small |
| 7 | Dissection Table (block + recipes) | ❌ Not started | Medium |
| 8 | Altar 3×3 attachment grid UI | ⚠️ Mock only | Very Large |
| 9 | Ragdoll topology from grid → TransformHierarchy | ⚠️ Star topo exists | Large |
| 10 | Static walk animation (StaticGaitAnimator) | ❌ Not started | Large |
| 11 | Ground contact detection per bodypart | ❌ Not started | Medium |
| 12 | Operation Table full implementation | ⚠️ Shell only | Large |
| 13 | Dev block part selector + socket editor polish | ⚠️ Mostly done | Small |
| 14 | Bodypart config validation gate wiring | ⚠️ Partial | Small |
| 15 | Ship validated JSON configs for all adapters | ❌ Not started | Medium |
| 16 | All integration pass (interconnect everything) | ❌ Not started | Large |
| 17 | Placeholder textures for new items | ❌ Not started | Small |
| 18 | Sounds + sound event registration | ❌ Not started | Medium |
| 19 | Grim Codex chapter content | ⚠️ Stubs only | Medium |
| 20 | JEI categories for new blocks | ❌ Not started | Small |
| 21 | Advancements (8 new) | ❌ Not started | Small |
| 22 | Cemetery jigsaw structure | ❌ Not started | Medium |
| 23 | Combat perks (warden boom, blaze jets, turret) | ❌ Not started | Medium |
| 24 | OBJ special scythe (opt-in) | ⚠️ Config gate exists | Small |
| 25 | Bug fixes (F06 replication, pivot bug, PigZombie) | ⚠️ Documented | Medium |
| 26 | Release checklist gates | ❌ Not started | Small |
