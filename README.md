# Necromancy — Multi-Version Port

Port of [AtomicStryker's Necromancy mod](https://github.com/AtomicStryker/atomicstrykers-minecraft-mods/tree/1.7.10/Necromancy)
(originally by sirolf2009, Minecraft 1.7.10) to modern Minecraft versions.

---

## Repository Layout

```
necromancy/
├── legacy/                  ← Codebase A: Minecraft 1.12.2 (Forge)
├── modern/                  ← Codebase B: Minecraft 1.21.1 (Forge + NeoForge via Architectury)
│   └── backport-configs/    ← gradle.properties files + guide for 1.16.5 / 1.18.2 / 1.20.1
├── original_1710_src/       ← Read-only reference copy of the 1.7.10 source
└── original_1710_build.gradle
```

---

## Building

### Legacy (1.12.2)

Requires JDK 8.

```bash
cd legacy
./gradlew build
# Output: legacy/build/libs/necromancy-<version>.jar
```

### Modern (1.21.1 — primary target)

Requires JDK 21.

```bash
cd modern
./gradlew :forge:build          # Forge JAR
./gradlew :neoforge:build       # NeoForge JAR
```

### Modern (backport to older versions)

See `modern/backport-configs/BACKPORT_GUIDE.md` for step-by-step instructions and
a full API compatibility matrix covering 1.16.5, 1.18.2, and 1.20.1.

---

## Features

| Feature                    | Legacy (1.12.2) | Modern (1.16.5 – 1.21.1) |
|----------------------------|-----------------|--------------------------|
| Summoning Altar            | ✅              | ✅                       |
| Sewing Machine             | ✅              | ✅                       |
| Minion entity (follow/sit) | ✅              | ✅ (Goal-based AI)       |
| 17 mob types (NecroAPI)    | ✅              | ✅ (NecroEntityRegistry) |
| Isaac / NightCrawler/Teddy | ✅              | ✅                       |
| Blood Scythe / Bone Scythe | ✅              | ✅                       |
| Organs (food items)        | ✅              | ✅                       |
| Blood bucket / fluid       | ✅              | ✅                       |
| Necronomicon               | ✅              | ✅                       |
| World gen (blood chalices) | ✅              | ✅ (data-pack BiomeModifier) |
| Village cemetery component | ✅              | Planned                  |
| Achievements               | ✅              | Planned (Advancements)   |

---

## Architecture

### Codebase A — Legacy (1.12.2 Forge)

Direct port from 1.7.10. Key API changes applied:
- `cpw.mods.fml.*` → `net.minecraftforge.fml.*`
- `DataWatcher` → `EntityDataManager` with static `DataParameter` keys
- UUID-based taming (replaces string owner names)
- `IInventory` updated to 1.12.2 interface
- `SoundEvent` / `ResourceLocation` for sounds
- `EntityAIAttackMelee`, `EntityAIWanderAvoidWater` for entity AI

### Codebase B — Modern (Architectury, 1.16.5 – 1.21.1)

Near-complete rewrite using modern APIs. Highlights:
- Single `common` module — Architectury abstracts Forge / NeoForge differences
- `DeferredRegister` pattern for all registries
- `SynchedEntityData` with typed `EntityDataAccessor` keys
- `PathfinderMob` + `Goal`-based AI (FollowOwner, MeleeAttack, Sit, RangedAttack)
- Data-driven world gen via JSON configured/placed features + `BiomeModifier`
- Architectury `NetworkManager` for client↔server packets
- GeckoLib for entity rendering (replaces legacy `ModelRenderer`)
