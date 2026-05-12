package com.sirolf2009.necromancy;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Client-only configuration (HUD / overlays).
 */
public final class NecromancyClientConfig {

    public static final ModConfigSpec SPEC;

    /** Append multipart telemetry summary lines when the F3 debug screen is open. */
    public static final ModConfigSpec.BooleanValue MULTIPART_TELEMETRY_F3_LINES;

    /** Always-on compact multipart telemetry HUD (top-left). */
    public static final ModConfigSpec.BooleanValue MULTIPART_TELEMETRY_CORNER_HUD;

    /** Sample previous multipart poses on minions for render interpolation (client-only meaningful path). */
    public static final ModConfigSpec.BooleanValue MINION_MULTIPART_INTERPOLATION_CAPTURE;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        b.push("multipart");
        MULTIPART_TELEMETRY_F3_LINES = b
            .comment(
                "Append multipart telemetry lines to the F3 debug text panel.",
                "Works together with common.toml multipart profiling flags — pure clients only see receive-side net stats.")
            .define("telemetryF3Lines", false);
        MULTIPART_TELEMETRY_CORNER_HUD = b
            .comment("Draw a compact multipart telemetry HUD in the top-left corner.")
            .define("telemetryCornerHud", false);
        MINION_MULTIPART_INTERPOLATION_CAPTURE = b
            .comment(
                "Optional render-side pose snapshots for minion multipart smoothing (mirrors RootMobEntity.multipartPoseInterpolationCapture).",
                "Requires integrated/local configs so bodypart JSON resolves on the client — pure MP clients stay inert until replication ships.")
            .define("minionMultipartInterpolationCapture", false);
        b.pop();
        SPEC = b.build();
    }

    private NecromancyClientConfig() {}
}
