package com.sirolf2009.necromancy.client;

import com.sirolf2009.necromancy.Reference;
import com.sirolf2009.necromancy.item.ItemDevKnob;
import com.sirolf2009.necromancy.network.payload.DevKnobScrollPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Intercepts mouse-scroll events when the player holds a {@link ItemDevKnob} outside any GUI.
 *
 * <p>Axis selection rules (dominant component of the player's view vector):
 * <ul>
 *   <li>|Y| is largest → axis 1 (Y / pitch)</li>
 *   <li>|X| is largest → axis 0 (X / yaw)</li>
 *   <li>otherwise → axis 2 (Z / roll)</li>
 * </ul>
 *
 * <p>Step size per scroll notch:
 * <ul>
 *   <li>No modifiers: 0.1</li>
 *   <li>1 modifier (Shift OR Ctrl OR Alt): 0.01</li>
 *   <li>2 modifiers: 0.001</li>
 *   <li>All 3: 0.0001</li>
 * </ul>
 */
@EventBusSubscriber(modid = Reference.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class DevKnobScrollHandler {

    private static final double STEP_COARSE  = 0.1;
    private static final double STEP_FINE    = 0.01;
    private static final double STEP_FINER   = 0.001;
    private static final double STEP_FINEST  = 0.0001;

    private DevKnobScrollHandler() {}

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        Minecraft mc = Minecraft.getInstance();
        // Only intercept while no GUI is open
        if (mc.player == null || mc.screen != null) return;

        ItemStack held = mc.player.getMainHandItem();
        if (!(held.getItem() instanceof ItemDevKnob)) {
            held = mc.player.getOffhandItem();
            if (!(held.getItem() instanceof ItemDevKnob)) return;
        }

        // Consume the scroll so the hotbar selection doesn't change
        event.setCanceled(true);

        double scrollY = event.getScrollDeltaY();
        double scrollX = event.getScrollDeltaX();
        double raw = scrollY != 0 ? scrollY : scrollX;
        if (raw == 0) return;

        int mode = ItemDevKnob.getMode(held);
        BlockPos blockPos = ItemDevKnob.getBlockPos(held);
        int axis = dominantAxis(mc.player.getViewVector(1.0f));
        double step = stepFromModifiers();
        double delta = raw * step;

        PacketDistributor.sendToServer(new DevKnobScrollPayload(blockPos, mode, axis, delta));
    }

    /** Returns 0 (X), 1 (Y), or 2 (Z) for the dominant component of {@code v}. */
    private static int dominantAxis(Vec3 v) {
        double ax = Math.abs(v.x);
        double ay = Math.abs(v.y);
        double az = Math.abs(v.z);
        if (ay > ax && ay > az) return 1;
        if (ax >= az) return 0;
        return 2;
    }

    /**
     * Returns step size based on how many of Shift/Ctrl/Alt are held.
     * 0 modifiers → 0.1, 1 → 0.01, 2 → 0.001, 3 → 0.0001.
     */
    private static double stepFromModifiers() {
        int count = 0;
        if (Screen.hasShiftDown()) count++;
        if (Screen.hasControlDown()) count++;
        if (Screen.hasAltDown()) count++;
        return switch (count) {
            case 1 -> STEP_FINE;
            case 2 -> STEP_FINER;
            case 3 -> STEP_FINEST;
            default -> STEP_COARSE;
        };
    }
}
