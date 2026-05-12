#!/usr/bin/env python3
"""Generate Patchouli assets for necromancy:grim_codex (run from repo: python3 scripts/generate_grim_codex.py)."""
from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
OUT_CAT = ROOT / "src/main/resources/assets/necromancy/patchouli_books/grim_codex/en_us/categories"
OUT_ENT = ROOT / "src/main/resources/assets/necromancy/patchouli_books/grim_codex/en_us/entries"


def dump(path: Path, obj: object) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(obj, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")


CATEGORIES = [
    (
        "covenant",
        "Covenant & Reader",
        "Prefaces, warnings, and how to read this codex beside your altar.",
        "necromancy:necronomicon",
    ),
    (
        "ichor",
        "Ichor, Souls, Organs",
        "Everything that leaks, screams, or steams before it becomes a minion.",
        "necromancy:jar_of_blood",
    ),
    (
        "tools",
        "Instruments of the Trade",
        "Needles, blades, buckets, and the tome that hears your orders.",
        "necromancy:bone_needle",
    ),
    (
        "sepulcher",
        "Sepulcher Architecture",
        "Blocks that cage fluids, pace stitches, or pin souls to the overworld.",
        "necromancy:altar",
    ),
    (
        "seamstress",
        "Seamstress Arts",
        "The four-by-four grid where corpses become spawn eggs.",
        "necromancy:sewing_machine",
    ),
    (
        "invocation",
        "Invocation",
        "The Summoning Altar's contracts — offerings, limbs, and previews.",
        "necromancy:spawner",
    ),
    (
        "dominion",
        "Dominion",
        "Giving verbs to stitched servants once they walk.",
        "necromancy:brain_on_a_stick",
    ),
    (
        "bestiary",
        "Stitched Bestiary",
        "Eggs, teddy shortcuts, and where soul hearts enter the recipe.",
        "necromancy:minion_spawn_egg",
    ),
    (
        "corpus",
        "Corpus Catalog",
        "Every donor species the mod harvests — read limbs before you knit.",
        "necromancy:zombie_torso",
    ),
    (
        "appendix",
        "Appendices",
        "Pack wisdom: traders, recipe viewers, and stray commands.",
        "minecraft:writable_book",
    ),
]

SADDLE_TORSO = {"Cow", "Pig", "Spider", "Squid", "Camel"}

MOBS: list[tuple[str, str, str, str, str]] = [
    # slug, title, spotlight_item, anatomy paragraph, stitch notes
    (
        "cow",
        "Cow",
        "necromancy:cow_head",
        "Provides Head, Torso, Arm, and Legs tokens. $(rev)Cow$(warn) pieces skew toward slow quadruped locomotion when legs carry the profile.",
        "$(aside)Torso implements saddling — ride your minion like livestock once saddled.$(br2)"
        "Harvest pieces like any donor mob; verify sewing compatibility in JEI before wasting organs.",
    ),
    (
        "creeper",
        "Creeper",
        "necromancy:creeper_torso",
        "Torso and Legs only — $(rev)no head token$(warn). Expect hiss-adjacent sillhouettes when paired with humanoid heads elsewhere.",
        "Torso/legs-only donors excel when another adapter supplies head voice — experiment carefully.",
    ),
    (
        "enderman",
        "Enderman",
        "necromancy:enderman_head",
        "Full biped set: Head, Torso, Arms, Legs.",
        "$(rev)Head:$(warn) may blink the wearer away when struck — excellent panic button, risky near cliffs.",
    ),
    (
        "pig",
        "Pig",
        "necromancy:pig_head",
        "Full quadruped coverage.",
        "$(aside)Torso is saddled — pigs remain iconic mounts for stitched mash-ups.",
    ),
    (
        "pigzombie",
        "Zombified Piglin",
        "necromancy:pigzombie_head",
        "Head, Torso, Arms, Legs — nether-tainted humanoid pig hybrid.",
        "Pairs beautifully with blaze rods or skeleton limbs for themed brutes.",
    ),
    (
        "skeleton",
        "Skeleton",
        "necromancy:skeleton_torso",
        "Torso, Arms, Legs — skull belongs to $(italic)other$(warn) mods; use a donor head if you want a faced minion.",
        "$(rev)Arms:$(warn) grant an extra arrow after melee swings — volley harassers adore these grafts.",
    ),
    (
        "spider",
        "Spider",
        "necromancy:spider_head",
        "Head, Torso, Legs.",
        "$(aside)Torso accepts saddles — surprise arachnid cavalry.$(br2)Leg tokens dictate skittering locomotion profiles.",
    ),
    (
        "zombie",
        "Zombie",
        "necromancy:zombie_torso",
        "Torso, Arms, Legs — the baseline shambling donor.",
        "Ideal filler limbs when you crave classic green gait without rare drops.",
    ),
    (
        "chicken",
        "Chicken",
        "necromancy:chicken_head",
        "Full tiny biped coverage.",
        "Hop-heavy companions — legs transmit poultry locomotion to your assembly.",
    ),
    (
        "villager",
        "Villager",
        "necromancy:villager_head",
        "Complete biped limbs.",
        "Trade for exotic parts with wandering necromancers — see Appendix.",
    ),
    (
        "witch",
        "Witch",
        "necromancy:witch_head",
        "Full kit — hat silhouette optional, misery guaranteed.",
        "Splash-style hybrids love witch legs mixed with tankier torsos.",
    ),
    (
        "squid",
        "Squid",
        "necromancy:squid_head",
        "Head, Torso, Legs.",
        "$(aside)Saddled torso$(warn) plus squid legs yields swimmers — dry land leaves them sluggish.",
    ),
    (
        "cavespider",
        "Cave Spider",
        "necromancy:cavespider_head",
        "Head, Torso, Legs — compact venomous donor.",
        "Blend with beefier mobs when you need poison aesthetics without bulk.",
    ),
    (
        "sheep",
        "Sheep",
        "necromancy:sheep_head",
        "Full quadruped set.",
        "Woolly grafts pair with goat legs for alpine comedians.",
    ),
    (
        "irongolem",
        "Iron Golem",
        "necromancy:irongolem_head",
        "Full heavy biped analog.",
        "Torso and limbs skew toward slow, crushing locomotion — anchor tanks.",
    ),
    (
        "wolf",
        "Wolf",
        "necromancy:wolf_head",
        "$(rev)Head token only$(warn). No torso/legs means extremely constrained assemblies.",
        "$(aside)Lone-head previews echo spirits anchored without bodies — pair with foreign limbs deliberately.",
    ),
    (
        "allay",
        "Allay",
        "necromancy:allay_head",
        "Head, Torso, Arm — $(rev)no dedicated legs$(warn).",
        "Expect ARMS_AS_LEGS pivots or borrowed legs from another donor.",
    ),
    (
        "axolotl",
        "Axolotl",
        "necromancy:axolotl_head",
        "Head, Torso, Legs — amphibious locomotion profiles.",
        "Combine with squid legs for meme-tier swimmers.",
    ),
    (
        "camel",
        "Camel",
        "necromancy:camel_head",
        "Head, Torso, Legs — tall quadruped locomotion.",
        "$(aside)Torso supports saddles$(warn); desert raider vibes abound.",
    ),
    (
        "frog",
        "Frog",
        "necromancy:frog_head",
        "Full limb coverage including arms.",
        "Hop locomotion surfaces often — excellent gap closers.",
    ),
    (
        "rabbit",
        "Rabbit",
        "necromancy:rabbit_head",
        "Head, Torso, Legs — twitchy hop donor.",
        "Mix with skeleton arms for cartoonish bolt-throwers.",
    ),
    (
        "sniffer",
        "Sniffer",
        "necromancy:sniffer_head",
        "Head, Torso, Legs — chunky extinct locomotion.",
        "Pairs well with turtle-paced tanks when blending adapters.",
    ),
    (
        "warden",
        "Warden",
        "necromancy:warden_head",
        "Complete biped horror kit.",
        "Darkness sensors not ported verbatim — treat pieces as prestige trophies.",
    ),
    (
        "blaze",
        "Blaze",
        "necromancy:blaze_head",
        "Head plus blaze rod torso cluster — $(rev)no arm/legs tokens$(warn).",
        "$(rev)Torso:$(warn) smothers fire while worn — exceptional defensive graft.",
    ),
    (
        "goat",
        "Goat",
        "necromancy:goat_head",
        "Head, Torso, Legs — mountain hop locomotion.",
        "Ram-ready silhouettes; blend with cow torso for chaotic dairy.",
    ),
]


def corpus_entry(category: str, slug: str, title: str, icon: str, anatomy: str, notes: str) -> dict:
    saddle = ""
    if title in SADDLE_TORSO:
        saddle = "$(rev)Saddle-capable torso:$(warn) shears strip saddles like vanilla livestock — consult Dominion chapter for riding notes.$(br2)"
    _ = slug  # reserved if future deep-links reference filenames
    return {
        "name": title,
        "icon": icon,
        "category": f"necromancy:{category}",
        "pages": [
            {
                "type": "patchouli:spotlight",
                "item": icon,
                "title": title,
                "text": "$(rev)Corpus entry:$(warn) fragments torn from "
                + title
                + " donors keep their cultural locomotion hints even when stitched elsewhere.",
            },
            {"type": "patchouli:text", "title": "Recorded anatomy", "text": anatomy},
            {
                "type": "patchouli:text",
                "title": "Stitcher's marginalia",
                "text": saddle + notes,
            },
            {
                "type": "patchouli:relations",
                "title": "Continue reading",
                "entries": [
                    "necromancy:seamstress/grid_liturgy",
                    "necromancy:invocation/offering_schema",
                ],
            },
        ],
    }


STATIC_ENTRIES: list[tuple[str, str, dict]] = [
    (
        "covenant",
        "oath_of_threads",
        {
            "name": "Oath of Threads",
            "icon": "necromancy:necronomicon",
            "category": "necromancy:covenant",
            "sortnum": 0,
            "pages": [
                {
                    "type": "patchouli:text",
                    "title": "Reader be bound",
                    "text": "You opened the $(rev)Grim Codex$(warn) willingly. Every recipe herein assumes you accept bodily autonomy violations, soul taxation, and the laughter of stitched things at dusk.$(br2)"
                    "This book mirrors how $(italic)Lexica Botania$(warn) teaches layered magic: categories fan outward, entries deepen inward, and cross-links stitch chapters together.",
                },
                {
                    "type": "patchouli:text",
                    "title": "How chapters breathe",
                    "text": "$(bold)Covenant & Reader$(warn) — ethics + navigation.$(br)"
                    "$(bold)Ichor$(warn) tracks fluids and organs.$(br)"
                    "$(bold)Instruments$(warn) covers actionable tools.$(br)"
                    "$(bold)Sepulcher$(warn) explains blocks.$(br)"
                    "$(bold)Seamstress$(warn) + $(bold)Invocation$(warn) twin the sewing grid and altar ritual.$(br)"
                    "$(bold)Dominion$(warn) teaches orders.$(br)"
                    "$(bold)Bestiary$(warn) lists outputs.$(br)"
                    "$(bold)Corpus Catalog$(warn) enumerates donor species.$(br)"
                    "$(bold)Appendix$(warn) hosts meta guidance.",
                },
                {
                    "type": "patchouli:text",
                    "title": "Reload like a lexicographer",
                    "text": "Patchouli hot-reloads entries when you $(bold)Shift-click the pencil$(warn) in the book GUI — keep this codex open while editing JSON.$(br2)"
                    "$(aside)Developer tip:$(warn) bind JEI favorites for questionable organs.",
                },
            ],
        },
    ),
    (
        "covenant",
        "truth_of_jei",
        {
            "name": "JEI as Second Sight",
            "icon": "minecraft:spyglass",
            "category": "necromancy:covenant",
            "sortnum": 1,
            "pages": [
                {
                    "type": "patchouli:text",
                    "title": "Why JEI matters",
                    "text": "Install $(rev)Just Enough Items$(warn). The mod exposes:$(br2)"
                    "• Every vanilla crafting recipe we ship as datapacks.$(br)"
                    "• A dedicated $(bold)Sewing Machine$(warn) category mirroring the 4×4 flesh grid.$(br)"
                    "• A $(bold)Necromancy Handbook$(warn) tab with prose blurbs per item.$(br2)"
                    "This codex explains $(italic)why$(warn); JEI shows $(italic)exact ingredient matrices$(warn).",
                },
                {
                    "type": "patchouli:relations",
                    "title": "Hop sideways",
                    "entries": [
                        "necromancy:appendix/recipe_viewers",
                        "necromancy:seamstress/grid_liturgy",
                    ],
                },
            ],
        },
    ),
    (
        "ichor",
        "blood_circulation",
        {
            "name": "Blood Circulation",
            "icon": "necromancy:bucket_blood",
            "category": "necromancy:ichor",
            "sortnum": 0,
            "pages": [
                {
                    "type": "patchouli:text",
                    "title": "Hydraulic theology",
                    "text": "Blood is both fluid and currency. Source blocks behave like thin lava visually but obey mod-specific collision rules — bucket them like water sources once you own $(rev)Bucket of Blood$(warn).$(br2)"
                    "Blood fills altar contracts, converts into jars for rituals, and stains Isaac-themed ammunition.",
                },
                {
                    "type": "patchouli:spotlight",
                    "item": "necromancy:jar_of_blood",
                    "title": "Jar of Blood",
                    "text": "Portable offering unit accepted $(bold)only$(warn) by the Summoning Altar's blood slot.",
                },
                {
                    "type": "patchouli:crafting",
                    "recipe": "necromancy:bucket_blood",
                    "recipe2": "necromancy:jar_of_blood_from_bucket",
                    "title": "Logistics chain",
                },
            ],
        },
    ),
    (
        "ichor",
        "soul_taxation",
        {
            "name": "Soul Taxation",
            "icon": "necromancy:soul_in_a_jar",
            "category": "necromancy:ichor",
            "sortnum": 1,
            "pages": [
                {
                    "type": "patchouli:text",
                    "title": "Capturing willing screams",
                    "text": "Souls enter jars when $(rev)eligible scythe kills$(warn) convert pity into inventory clutter. Stockpile jars — altars demand one beside blood.$(br2)"
                    "Soul jars also participate in crafting soul hearts at the sewing grid.",
                },
                {
                    "type": "patchouli:spotlight",
                    "item": "necromancy:soul_in_a_jar",
                    "title": "Soul in a Jar",
                    "text": "Catalyst slot opposite blood on the altar. Without both jars the altar sulks.",
                },
            ],
        },
    ),
    (
        "ichor",
        "organ_quintet",
        {
            "name": "Quintet of Organs",
            "icon": "necromancy:heart",
            "category": "necromancy:ichor",
            "sortnum": 2,
            "pages": [
                {
                    "type": "patchouli:text",
                    "title": "Five edible horrors",
                    "text": "$(rev)Brains, Heart, Muscle, Lungs, Skin$(warn) drop from compatible corpses via loot tables tuned like the legacy mod.$(br2)"
                    "They taste foul but stitch true — each organ occupies sewing macros reminiscent of Botania rune recipes: predictable slots, thematic outputs.",
                },
                {
                    "type": "patchouli:spotlight",
                    "item": "necromancy:brains",
                    "title": "Brains",
                    "text": "Greasy stuffing for spawn eggs.",
                },
                {
                    "type": "patchouli:spotlight",
                    "item": "necromancy:heart",
                    "title": "Heart",
                    "text": "Anchors soul-heart weaving.",
                },
                {
                    "type": "patchouli:spotlight",
                    "item": "necromancy:muscle",
                    "title": "Muscle",
                    "text": "Bulk tissue bridging limb clusters.",
                },
            ],
        },
    ),
    (
        "ichor",
        "skin_leather_symmetry",
        {
            "name": "Skin ↔ Leather",
            "icon": "necromancy:skin",
            "category": "necromancy:ichor",
            "sortnum": 3,
            "pages": [
                {
                    "type": "patchouli:text",
                    "title": "Industrial flaying",
                    "text": "Throw leather through the sewing needle's logic to unwrap eight $(rev)Skin$(warn) sheets — identical conversion rate to the historic port.",
                },
                {
                    "type": "patchouli:text",
                    "title": "Why stacks matter",
                    "text": "Skin wraps entire limb bundles before Soul Hearts finalize an egg — shortages stall mid-recipe like forgetting Botania runes mid-apothecary craft.",
                },
            ],
        },
    ),
    (
        "tools",
        "needle_absolution",
        {
            "name": "Bone Needle",
            "icon": "necromancy:bone_needle",
            "category": "necromancy:tools",
            "sortnum": 0,
            "pages": [
                {
                    "type": "patchouli:text",
                    "title": "Sacrament of puncture",
                    "text": "The sewing machine refuses greedy fingers — only bone needles channel mana-like durability drains while knitting corpses.",
                },
                {
                    "type": "patchouli:crafting",
                    "recipe": "necromancy:bone_needle",
                },
            ],
        },
    ),
    (
        "tools",
        "scythe_gospels",
        {
            "name": "Scythe Pair",
            "icon": "necromancy:scythe",
            "category": "necromancy:tools",
            "sortnum": 1,
            "pages": [
                {
                    "type": "patchouli:text",
                    "title": "Harvest liturgy",
                    "text": "$(rev)Blood$(warn) and $(rev)Bone$(warn) scythes vary tiers but share the mandate: reap souls when kills qualify.",
                },
                {
                    "type": "patchouli:crafting",
                    "recipe": "necromancy:scythe",
                    "recipe2": "necromancy:scythe_bone",
                },
            ],
        },
    ),
    (
        "tools",
        "blood_bucket_manual",
        {
            "name": "Buckets & Basins",
            "icon": "necromancy:bucket_blood",
            "category": "necromancy:tools",
            "sortnum": 2,
            "pages": [
                {
                    "type": "patchouli:text",
                    "title": "Fluid logistics",
                    "text": "Buckets cart source blood across dimensions; combine with empty jars at crafting stations per datapack recipes.",
                },
                {
                    "type": "patchouli:crafting",
                    "recipe": "necromancy:bucket_blood",
                },
            ],
        },
    ),
    (
        "tools",
        "necronomicon_dualface",
        {
            "name": "Necronomicon",
            "icon": "necromancy:necronomicon",
            "category": "necromancy:tools",
            "sortnum": 3,
            "pages": [
                {
                    "type": "patchouli:text",
                    "title": "Two sacraments",
                    "text": "$(bold)On planks:$(warn) searches cardinal cobblestones to raise an altar triptych (altar block flanked by decorative cousin blocks).$(br2)"
                    "$(bold)In air:$(warn) opens dominion UI counting obedient minions and issuing Stay/Follow/Dismiss verbs.",
                },
                {
                    "type": "patchouli:crafting",
                    "recipe": "necromancy:necronomicon",
                },
                {
                    "type": "patchouli:relations",
                    "title": "Follow-up reading",
                    "entries": [
                        "necromancy:sepulcher/altar_genesis",
                        "necromancy:dominion/command_litany",
                    ],
                },
            ],
        },
    ),
    (
        "sepulcher",
        "altar_genesis",
        {
            "name": "Raising the Altar",
            "icon": "necromancy:altar",
            "category": "necromancy:sepulcher",
            "sortnum": 0,
            "pages": [
                {
                    "type": "patchouli:text",
                    "title": "Plank anchor ritual",
                    "text": "Stand on any vanilla plank column. Along one horizontal cardinal there must exist $(rev)two contiguous cobblestone blocks$(warn).$(br2)"
                    "Use the Necronomicon on the plank face; the mod replaces plank→altar while cobbles→altar cousin blocks, preserving facing.",
                },
                {
                    "type": "patchouli:text",
                    "title": "ASCII vigil",
                    "text": "[P][C][C] layout where P becomes the interactive altar facing outward and each C becomes cousin altar brick.$(br2)"
                    "Rotate yourself — each cardinal is legal provided geometry matches.",
                },
                {
                    "type": "patchouli:spotlight",
                    "item": "necromancy:altar_block",
                    "title": "Altar Block",
                    "text": "Cosmetic cousin — does not host GUI summoning alone.",
                },
            ],
        },
    ),
    (
        "sepulcher",
        "sewing_installation",
        {
            "name": "Sewing Machine",
            "icon": "necromancy:sewing_machine",
            "category": "necromancy:sepulcher",
            "sortnum": 1,
            "pages": [
                {
                    "type": "patchouli:text",
                    "title": "Industrial seamstress",
                    "text": "Place like smelters — opens a 4×4 crafting canvas plus needle slot. Wrong needles abort crafts silently like Botania apothecaries without petals.",
                },
                {
                    "type": "patchouli:crafting",
                    "recipe": "necromancy:sewing_machine",
                },
            ],
        },
    ),
    (
        "sepulcher",
        "trophy_wall",
        {
            "name": "Skull on the Wall",
            "icon": "necromancy:skull_wall",
            "category": "necromancy:sepulcher",
            "sortnum": 2,
            "pages": [
                {
                    "type": "patchouli:text",
                    "title": "Pure ambience",
                    "text": "Decoration block echoing dungeons — no gameplay coupling beyond vibes.",
                },
                {
                    "type": "patchouli:spotlight",
                    "item": "necromancy:skull_wall",
                    "title": "Skull Wall",
                    "text": "Pairs with vanilla skull trophies for haunted labs.",
                },
            ],
        },
    ),
    (
        "sepulcher",
        "blood_sea_notes",
        {
            "name": "Blood Seas",
            "icon": "necromancy:bucket_blood",
            "category": "necromancy:sepulcher",
            "sortnum": 3,
            "pages": [
                {
                    "type": "patchouli:text",
                    "title": "Fluid blocks",
                    "text": "Blood sources behave as custom fluids — no item block, only buckets + world pools.$(br2)"
                    "Worldgen may scatter chalices — explore cautiously.",
                },
                {
                    "type": "patchouli:relations",
                    "entries": ["necromancy:ichor/blood_circulation"],
                },
            ],
        },
    ),
    (
        "seamstress",
        "grid_liturgy",
        {
            "name": "Four-by-Four Grid",
            "icon": "necromancy:bone_needle",
            "category": "necromancy:seamstress",
            "sortnum": 0,
            "pages": [
                {
                    "type": "patchouli:text",
                    "title": "Square theology",
                    "text": "The sewing matrix mirrors Botania's crafting halo philosophy but skews square: sixteen slots swallow limbs like runic plates swallow petals.$(br2)"
                    "$(bold)Shapeless$(warn) recipes ignore orientation so long as multiset matches.$(br)"
                    "$(bold)Shaped$(warn) recipes honor rows exactly — teddy plush recipe forms a leather cradle around wool hearts.",
                },
                {
                    "type": "patchouli:text",
                    "title": "Needle etiquette",
                    "text": "Bone needles occupy their dedicated slot — durability drains per craft identical to legacy balancing.",
                },
                {
                    "type": "patchouli:text",
                    "title": "Where JEI enters",
                    "text": "Because dozens of donor-specific recipes exist, we mirror Botania's reliance on external lexicons:$(br2)"
                    "$(italic)Use JEI's Sewing category$(warn) — filter by output egg or ingredient chunk.",
                },
            ],
        },
    ),
    (
        "seamstress",
        "soul_heart_psalm",
        {
            "name": "Soul Heart Weaving",
            "icon": "necromancy:spawner",
            "category": "necromancy:seamstress",
            "sortnum": 1,
            "pages": [
                {
                    "type": "patchouli:text",
                    "title": "Heart of the stitch",
                    "text": "Spawn egg sewing macros orbit the $(rev)Soul Heart$(warn) item — equivalent to focusing Botania terrasteel gear around a single catalyst chip.",
                },
                {
                    "type": "patchouli:spotlight",
                    "item": "necromancy:spawner",
                    "title": "Soul Heart item",
                    "text": "Crafted via rotten surplus + ghast tears + soul jar + heart shapeless macro — identical ratios to ancient docs.",
                },
                {
                    "type": "patchouli:text",
                    "title": "Ingredients reminder",
                    "text": "5× Rotten Flesh$(br)"
                    "2× Ghast Tear$(br)"
                    "Soul Jar + Heart$(br2)"
                    "JEI shows slot placements exactly.",
                },
            ],
        },
    ),
    (
        "seamstress",
        "teddy_lullaby",
        {
            "name": "Teddy Plush Macro",
            "icon": "necromancy:teddy_spawn_egg",
            "category": "necromancy:seamstress",
            "sortnum": 2,
            "pages": [
                {
                    "type": "patchouli:text",
                    "title": "Soft horror",
                    "text": "Surround double wool souls with leather curtain walls — recipe is $(bold)shaped$(warn). Memorize the mantra:$(br2)"
                    "LLLL / LWWL / LWWL / LLLL",
                },
                {
                    "type": "patchouli:text",
                    "title": "Why shaped matters",
                    "text": "Misplaced leather yields nothing — treat it like Botania's floral obsession with positional purity.",
                },
                {
                    "type": "patchouli:relations",
                    "entries": ["necromancy:bestiary/teddy_manifest"],
                },
            ],
        },
    ),
    (
        "invocation",
        "offering_schema",
        {
            "name": "Altar Slot Schema",
            "icon": "necromancy:altar",
            "category": "necromancy:invocation",
            "sortnum": 0,
            "pages": [
                {
                    "type": "patchouli:text",
                    "title": "Seven focal slots",
                    "text": "$(bold)Slot 0:$(warn) Jar of Blood — only blood jars accepted.$(br)"
                    "$(bold)Slot 1:$(warn) Soul jar catalyst opposite blood.$(br)"
                    "$(bold)Slots 2-6:$(warn) Head, Torso, Legs, Right Arm, Left Arm — filters reject misplaced anatomy.",
                },
                {
                    "type": "patchouli:text",
                    "title": "Why duplicates matter",
                    "text": "Arm items tagged $(italic)ArmLeft$(warn) accept either lateral slot — adapters overload metadata like Botania flowers sharing textures.",
                },
            ],
        },
    ),
    (
        "invocation",
        "preview_resurrection",
        {
            "name": "Preview Rites",
            "icon": "minecraft:ender_eye",
            "category": "necromancy:invocation",
            "sortnum": 1,
            "pages": [
                {
                    "type": "patchouli:text",
                    "title": "Phantom assemblies",
                    "text": "Altar preview mimics actual $(rev)MinionAssembly$(warn) resolution — observe locomotion flags before burning jars.",
                },
                {
                    "type": "patchouli:text",
                    "title": "Combinatorial explosion",
                    "text": "Listing every summon like Botania lists every flower variant is impossible — rely on sewing recipes + intuition.$(br2)"
                    "$(aside)This codex refuses altar permutation tables on purpose.",
                },
            ],
        },
    ),
    (
        "invocation",
        "locomotion_mysteries",
        {
            "name": "Locomotion Mysteries",
            "icon": "necromancy:zombie_legs",
            "category": "necromancy:invocation",
            "sortnum": 2,
            "pages": [
                {
                    "type": "patchouli:text",
                    "title": "Standing mode",
                    "text": "Legs slot filled → donor legs dictate gait, speed multiplier, swim quirks.",
                },
                {
                    "type": "patchouli:text",
                    "title": "Arms-as-legs heresy",
                    "text": "No legs but torso + arms → arms pretend to be legs, lifting torso, halving stride — documented in internal assembly resolver identical to legacy ARMS_AS_LEGS.",
                },
                {
                    "type": "patchouli:text",
                    "title": "Static idols",
                    "text": "Missing critical anatomy yields STATIC profile — perfect for trophy husks.",
                },
            ],
        },
    ),
    (
        "dominion",
        "command_litany",
        {
            "name": "Command Litany",
            "icon": "necromancy:necronomicon",
            "category": "necromancy:dominion",
            "sortnum": 0,
            "pages": [
                {
                    "type": "patchouli:text",
                    "title": "Radial verbs",
                    "text": "$(bold)Stay$(warn) roots minions.$(br)"
                    "$(bold)Follow$(warn) resumes tailing.$(br)"
                    "$(bold)Dismiss$(warn) severs contracts.$(br2)"
                    "Counts display only obedient entities in range — spam responsibly.",
                },
                {
                    "type": "patchouli:text",
                    "title": "Ownership ghosts",
                    "text": "Dismiss clears persistence similar to revoking Botania corporea networks — backup saves before experiments.",
                },
            ],
        },
    ),
    (
        "dominion",
        "brain_rod",
        {
            "name": "Brain on a Stick",
            "icon": "necromancy:brain_on_a_stick",
            "category": "necromancy:dominion",
            "sortnum": 1,
            "pages": [
                {
                    "type": "patchouli:text",
                    "title": "Macro leash",
                    "text": "Brandish near loyal servants to steer AI priorities — reminiscent of carrot-on-stick semantics but horrifying.",
                },
                {
                    "type": "patchouli:crafting",
                    "recipe": "necromancy:brain_on_a_stick",
                },
            ],
        },
    ),
    (
        "dominion",
        "saddle_doctrine",
        {
            "name": "Saddle Doctrine",
            "icon": "minecraft:saddle",
            "category": "necromancy:dominion",
            "sortnum": 2,
            "pages": [
                {
                    "type": "patchouli:text",
                    "title": "Which thoraxes kneel",
                    "text": "$(rev)Cow, Pig, Spider, Squid, Camel$(warn) torsos expose saddling hooks via ISaddleAble adapters.$(br2)"
                    "Right-click saddle to mount if you're recognized owner — shears strip saddles gently.",
                },
                {
                    "type": "patchouli:text",
                    "title": "Rendering clues",
                    "text": "Borrowed saddle textures overlay borrowed geometry — mismatched mob frankensteins still display saddles when torso donor qualifies.",
                },
            ],
        },
    ),
    (
        "bestiary",
        "egg_manifest",
        {
            "name": "Spawn Egg Manifest",
            "icon": "necromancy:minion_spawn_egg",
            "category": "necromancy:bestiary",
            "sortnum": 0,
            "pages": [
                {
                    "type": "patchouli:text",
                    "title": "Eggs as receipts",
                    "text": "Successful sewing yields eggs referencing:$(br)"
                    "• Generic stitched $(rev)Minions$(warn)$(br)"
                    "• Specialist $(rev)Night Crawlers$(warn), $(rev)Isaac$(warn), plush $(rev)Teddies$(warn).",
                },
                {
                    "type": "patchouli:spotlight",
                    "item": "necromancy:nightcrawler_spawn_egg",
                    "title": "Night Crawler Egg",
                    "text": "Summons stalking horror mob.",
                },
                {
                    "type": "patchouli:spotlight",
                    "item": "necromancy:isaac_spawn_egg",
                    "title": "Isaac Egg",
                    "text": "Summons Issac-themed antagonists — consult Isaac chapter.",
                },
            ],
        },
    ),
    (
        "bestiary",
        "teddy_manifest",
        {
            "name": "Teddy Manifest",
            "icon": "necromancy:teddy_spawn_egg",
            "category": "necromancy:bestiary",
            "sortnum": 1,
            "pages": [
                {
                    "type": "patchouli:text",
                    "title": "Comfort and teeth",
                    "text": "Teddy eggs bypass corpse donors — pure textile necromancy reminiscent of Botania's corporea dolls.",
                },
                {
                    "type": "patchouli:relations",
                    "entries": ["necromancy:seamstress/teddy_lullaby"],
                },
            ],
        },
    ),
    (
        "bestiary",
        "isaac_fragments",
        {
            "name": "Isaac Fragments",
            "icon": "necromancy:isaacs_severed_head",
            "category": "necromancy:bestiary",
            "sortnum": 2,
            "pages": [
                {
                    "type": "patchouli:text",
                    "title": "Headgear artillery",
                    "text": "$(rev)Isaac's Severed Head$(warn) fires tears while worn — binds to client keyhandler + sneak combos.",
                },
                {
                    "type": "patchouli:text",
                    "title": "Ammo taxonomy",
                    "text": "Plain $(rev)tear$(warn) vs $(rev)tear_blood$(warn) items correspond to projectile palettes — stockpile both.",
                },
                {
                    "type": "patchouli:spotlight",
                    "item": "necromancy:isaacs_severed_head_trophy",
                    "title": "Trophy Variant",
                    "text": "Vanity duplicate celebrating boss kills.",
                },
            ],
        },
    ),
    (
        "appendix",
        "recipe_viewers",
        {
            "name": "Recipe Viewers & APIs",
            "icon": "minecraft:compass",
            "category": "necromancy:appendix",
            "sortnum": 0,
            "pages": [
                {
                    "type": "patchouli:text",
                    "title": "Installed mods",
                    "text": "$(bold)JEI$(warn) exposes crafting + sewing categories we register programmatically.$(br2)"
                    "$(bold)Patchouli$(warn) hosts this codex — optional dependency but strongly recommended for apprentices.",
                },
                {
                    "type": "patchouli:text",
                    "title": "Future ports",
                    "text": "Both libraries existed near Botania-era Forge — migrating content down to 1.12 mostly means reshaping JSON accents while preserving narrative spine.",
                },
            ],
        },
    ),
    (
        "appendix",
        "villager_covenant",
        {
            "name": "Villager Traders",
            "icon": "minecraft:emerald",
            "category": "necromancy:appendix",
            "sortnum": 1,
            "pages": [
                {
                    "type": "patchouli:text",
                    "title": "Emerald anatomists",
                    "text": "Custom trades inject random body parts behind emerald tolls — revisit villages after slaughter for fresh graft RNG.",
                },
            ],
        },
    ),
    (
        "corpus",
        "catalog_preface",
        {
            "name": "Catalog Preface",
            "icon": "necromancy:cow_torso",
            "category": "necromancy:corpus",
            "sortnum": 0,
            "pages": [
                {
                    "type": "patchouli:text",
                    "title": "Why per-species entries",
                    "text": "Botania catalogs flowers individually — we catalog donors individually so you know $(italic)which trophies exist$(warn) before farming.",
                },
                {
                    "type": "patchouli:text",
                    "title": "Cross-reference ritual",
                    "text": "Each entry ends with relations pointing back to sewing + altar chapters — identical to Lexica cross-links pointing at Elven Gateway essays.",
                },
            ],
        },
    ),
]


def main() -> None:
    OUT_CAT.mkdir(parents=True, exist_ok=True)
    for slug, name, desc, icon in CATEGORIES:
        dump(OUT_CAT / f"{slug}.json", {"name": name, "description": desc, "icon": icon})

    seen: set[Path] = set()
    for cat, fname, payload in STATIC_ENTRIES:
        p = OUT_ENT / cat / f"{fname}.json"
        dump(p, payload)
        seen.add(p)

    sortnum = 10
    for slug, title, icon, anatomy, notes in MOBS:
        payload = corpus_entry("corpus", slug, title, icon, anatomy, notes)
        payload["sortnum"] = sortnum
        sortnum += 1
        p = OUT_ENT / "corpus" / f"{slug}.json"
        dump(p, payload)
        seen.add(p)

    print(f"Wrote {len(CATEGORIES)} categories and {len(seen)} entries under {OUT_ENT.parent}")


if __name__ == "__main__":
    main()
