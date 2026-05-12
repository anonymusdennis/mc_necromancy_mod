# Necromancy 1.21.1 - version 0.5 snapshot

Frozen snapshot of the mod immediately before the v0.5 framework refactor.

## What is included

- `modern/` -- a copy of the active workspace at this point in time (source,
  resources, gradle build files; excludes `build/`, `.gradle/`, `run/`).
- `necromancy-2.0.0-v0.5.jar` -- the built mod jar produced from the source
  in this folder (`./gradlew build` in `modern/`).

## What works in v0.5

- The Necromancy 1.21.1 NeoForge port runs on a vanilla NeoForge dev or
  shipping client.
- Full Summoning Altar flow: insert Jar of Blood + Soul in a Jar + body
  parts in their correctly typed slots, shift-rightclick to summon.
- Body-part assembly with proper inter-part anchors:
  - Quadruped torsos lay flat (cow, sheep, pig).
  - Spider/squid contribute eight legs and undulate correctly.
  - Creeper contributes four legs that trot in vanilla diagonal pairs.
  - Wolf contributes a head (and only a head).
- Sewing Machine 4x4 recipes, Blood fluid + Bucket of Blood, Necronomicon
  GUI, Bone Needle, Brain on a Stick, Soul-in-a-Jar, Jar of Blood,
  Scythe + Bone Scythe (rendered via custom BEWLR), Spawner item, Isaac's
  Head trophy and the Tear / TearBlood projectiles.
- Custom mobs: EntityMinion (tamable, follows owner, attacks Monsters),
  EntityNightCrawler, EntityTeddy and the four Isaac variants.
- Nether chalice worldgen + necromancer villager trades.

## Known issues frozen in this snapshot

These are the items the v0.5 framework refactor is meant to fix; they are
present in this snapshot:

- Body parts with a missing connector (e.g. arms with no torso) still
  render and can float.
- Minion movement speed is constant regardless of which legs adapter is
  attached -- a squid-legs minion zooms around like a zombie.
- No per-adapter step / ambient / hurt / death sounds -- everything
  speaks zombie.
- Quadruped "arms" (cow, pig, ...) inherit a walking animation even when
  a non-quadruped torso/legs combo is attached.
- No "arms-as-legs" fallback when an adapter has no legs: the minion
  drags through the ground instead of walking on its hands.
- No `PartFeature` framework yet; per-bodypart abilities (saddle, ranged
  attack, fire breath, ...) live in ad-hoc places (`ISaddleAble` etc.).

## How to use this snapshot

Drop `necromancy-2.0.0-v0.5.jar` into the `mods/` folder of any NeoForge
1.21.1 instance to run the frozen v0.5 build.  To build it from source:

```
cd version_0_point_5/modern
./gradlew build
```

The resulting jar will be at `version_0_point_5/modern/build/libs/`.
