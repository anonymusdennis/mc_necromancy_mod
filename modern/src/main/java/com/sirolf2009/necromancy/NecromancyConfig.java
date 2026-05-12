package com.sirolf2009.necromancy;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Common (server + client) configuration for the Necromancy mod.
 *
 * <p>The legacy 1.7.10 {@code ConfigurationNecromancy} class held three values:
 * <ul>
 *     <li>{@code RenderSpecialScythe} -- gate for the OBJ "special folk" scythe model.</li>
 *     <li>{@code rarityNightCrawlers} -- 1-in-N chance to replace a zombie at SpecialSpawn.</li>
 *     <li>{@code rarityIsaacs} -- 1-in-N chance to replace a skeleton at SpecialSpawn.</li>
 * </ul>
 * plus a server property {@code max_minion_spawn} (loaded directly from the
 * Minecraft server.properties on the original).  We promote that value into the
 * config spec so the "vanilla server.properties" hack is no longer needed.
 */
public final class NecromancyConfig {

    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue RENDER_SPECIAL_SCYTHE;
    public static final ModConfigSpec.IntValue     RARITY_NIGHT_CRAWLERS;
    public static final ModConfigSpec.IntValue     RARITY_ISAACS;
    public static final ModConfigSpec.IntValue     MAX_MINION_SPAWN;
    public static final ModConfigSpec.BooleanValue ENABLE_SPECIAL_FOLK_FETCH;

    /** Multipart subsystem telemetry — disabled by default (see multipart.enableMultipartTelemetry). */
    public static final ModConfigSpec.BooleanValue MULTIPART_TELEMETRY_ENABLED;
    public static final ModConfigSpec.BooleanValue MULTIPART_TELEMETRY_STRESS_LIGHTWEIGHT;

    /** When true, minions keep altar-era {@link com.sirolf2009.necromancy.bodypart.MinionCompositeCollision} for hitboxes instead of the multipart hierarchy. */
    public static final ModConfigSpec.BooleanValue MINION_LEGACY_COMPOSITE_COLLISION;

    /** Experimental — multipart limbs drain independent pools mirrored onto vanilla HP via {@link com.sirolf2009.necromancy.multipart.damage.MultipartHealthAggregate}. */
    public static final ModConfigSpec.BooleanValue MINION_MULTIPART_PER_PART_HEALTH;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();

        b.push("client");
        RENDER_SPECIAL_SCYTHE = b
            .comment("Render the OBJ-based 'special scythe' model for whitelisted players.")
            .define("renderSpecialScythe", true);
        ENABLE_SPECIAL_FOLK_FETCH = b
            .comment(
                "When true, fetch the legacy specialFolk.txt list at startup so",
                "the special scythe renderer activates for those player names.",
                "Disabled by default in 1.21.1 since it requires outbound HTTP.")
            .define("enableSpecialFolkFetch", false);
        b.pop();

        b.push("spawning");
        RARITY_NIGHT_CRAWLERS = b
            .comment("1-in-N chance for a zombie spawn to be replaced by a Night Crawler.")
            .defineInRange("rarityNightCrawlers", 30, 1, 1000);
        RARITY_ISAACS = b
            .comment("1-in-N chance for a skeleton spawn to be replaced by an Isaac.")
            .defineInRange("rarityIsaacs", 60, 1, 1000);
        b.pop();

        b.push("minions");
        MAX_MINION_SPAWN = b
            .comment(
                "Maximum number of minions a single player may have summoned.",
                "Set to -1 to disable the cap (matches the original behaviour).")
            .defineInRange("maxMinionSpawn", -1, -1, 4096);
        MINION_LEGACY_COMPOSITE_COLLISION = b
            .comment(
                "Use legacy five-slot yaw-only AABB assembly for minions instead of TransformHierarchy collision.",
                "Enable temporarily if multipart soak tests reveal regressions.",
                "Plan doc synonym: necromancy.multipart.minionLegacyCollision — TOML key remains minionLegacyCompositeCollision.")
            .define("minionLegacyCompositeCollision", false);
        MINION_MULTIPART_PER_PART_HEALTH = b
            .comment(
                "Route incoming damage through per-part hitboxes with independent HP pools (experimental).",
                "Requires multipart collision (minionLegacyCompositeCollision=false).")
            .define("minionMultipartPerPartHealth", false);
        b.pop();

        b.push("multipart");
        MULTIPART_TELEMETRY_ENABLED = b
            .comment(
                "Developer multipart profiling (transform propagation, broad-phase publishes, queries, topology edits, net payloads, activity transitions).",
                "Leave false on production servers — every hook short-circuits when disabled.")
            .define("enableMultipartTelemetry", false);
        MULTIPART_TELEMETRY_STRESS_LIGHTWEIGHT = b
            .comment(
                "When enableMultipartTelemetry is true: record aggregates only (skip per-entity buckets).",
                "Recommended for large multiplayer stress runs.")
            .define("stressTelemetryLightweight", false);
        b.pop();

        SPEC = b.build();
    }

    private NecromancyConfig() {}
}
