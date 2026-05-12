package com.sirolf2009.necromancy.gui;

/**
 * Shared layout constants for bodypart dev menu + screen (server-safe).
 */
public final class BodypartDevLayout {

    /** Wide enough for two columns of tabs and checkbox captions. */
    public static final int PANEL_WIDTH = 508;

    public static final int PANEL_HEIGHT = 392;

    public static final int EDITOR_LEFT = 12;

    public static final int RIGHT_MARGIN = 12;

    /** First row of tab buttons (relative to panel top). */
    public static final int TAB_ROW1_Y = 22;

    /** Second row of tab buttons. */
    public static final int TAB_ROW2_Y = TAB_ROW1_Y + 22;

    public static final int TAB_BUTTON_HEIGHT = 20;

    /**
     * Horizontal gap between the two tab columns.
     */
    public static final int TAB_COLUMN_GAP = 8;

    /**
     * Content area below tab rows (relative to panel top).
     */
    public static final int CONTENT_TOP = TAB_ROW2_Y + TAB_BUTTON_HEIGHT + 10;

    public static final int PLAYER_INV_FIRST_ROW_Y = 312;

    public static final int HOTBAR_EXTRA_GAP = 6;

    public static final int INVENTORY_LABEL_Y = 296;

    public static final int SLOT_PART_X = 10;

    public static final int SLOT_PART_Y = 6;

    /**
     * Width of each tab in the 2×2 grid: {@code (PANEL_WIDTH - EDITOR_LEFT - RIGHT_MARGIN - TAB_COLUMN_GAP) / 2}.
     */
    public static int tabButtonWidth() {
        return (PANEL_WIDTH - EDITOR_LEFT - RIGHT_MARGIN - TAB_COLUMN_GAP) / 2;
    }

    private BodypartDevLayout() {}
}
