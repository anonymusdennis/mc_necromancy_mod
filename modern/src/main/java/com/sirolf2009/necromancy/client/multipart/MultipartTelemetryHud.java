package com.sirolf2009.necromancy.client.multipart;

import com.sirolf2009.necromancy.NecromancyClientConfig;
import com.sirolf2009.necromancy.multipart.telemetry.MultipartTelemetry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import java.util.List;

/**
 * Developer overlays for {@link MultipartTelemetry}: optional corner HUD and F3 append lines.
 *
 * <p>Registered on the game event bus from {@link com.sirolf2009.necromancy.Necromancy}.
 */
public final class MultipartTelemetryHud {

    private static final int CORNER_COLOR = 0xFFE8E070;
    private static final int LINE_STEP = 10;

    private MultipartTelemetryHud() {}

    @SubscribeEvent
    public static void onRenderGuiPost(RenderGuiEvent.Post event) {
        if (!NecromancyClientConfig.MULTIPART_TELEMETRY_CORNER_HUD.get()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) {
            return;
        }
        GuiGraphics g = event.getGuiGraphics();
        List<String> lines = MultipartTelemetry.formatAggregateLines();
        int y = 8;
        for (String line : lines) {
            g.drawString(mc.font, line, 8, y, CORNER_COLOR, false);
            y += LINE_STEP;
        }
        if (mc.level != null) {
            g.drawString(mc.font,
                "dim=" + MultipartTelemetry.dimensionTag(mc.level),
                8,
                y,
                CORNER_COLOR,
                false);
        }
    }

    @SubscribeEvent
    public static void onDebugText(CustomizeGuiOverlayEvent.DebugText event) {
        if (!NecromancyClientConfig.MULTIPART_TELEMETRY_F3_LINES.get()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui || !mc.getDebugOverlay().showDebugScreen()) {
            return;
        }
        event.getLeft().add("");
        event.getLeft().addAll(MultipartTelemetry.formatAggregateLines());
        if (mc.level != null) {
            event.getLeft().add("multipart dim=" + MultipartTelemetry.dimensionTag(mc.level));
        }
    }
}
