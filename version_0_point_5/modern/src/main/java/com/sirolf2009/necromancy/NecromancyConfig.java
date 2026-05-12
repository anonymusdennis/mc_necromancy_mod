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
        b.pop();

        SPEC = b.build();
    }

    private NecromancyConfig() {}
}
