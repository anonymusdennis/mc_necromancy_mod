package com.sirolf2009.necromancy;

import net.minecraft.resources.ResourceLocation;

/**
 * Mod-wide string and {@link ResourceLocation} constants.
 *
 * <p>Mirrors {@code com.sirolf2009.necromancy.lib.ReferenceNecromancy} from the
 * 1.7.10 codebase, with paths updated for the 1.21.1 resource layout (no more
 * {@code textures/items/...} -- it is {@code textures/item/...} now).
 */
public final class Reference {

    public static final String MOD_ID   = "necromancy";
    public static final String MOD_NAME = "Necromancy";

    private Reference() {}

    /** Build a {@link ResourceLocation} in the {@code necromancy} namespace. */
    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    // ---------------------------------------------------------------------
    // Texture locations preserved from the original mod (paths kept exact
    // where possible so resource-pack overrides keep working).
    // ---------------------------------------------------------------------

    public static final ResourceLocation TEXTURES_GUI_ALTAR        = rl("textures/guis/altargui.png");
    public static final ResourceLocation TEXTURES_GUI_SEWING       = rl("textures/guis/sewinggui.png");

    /**
     * JEI sewing pane background — replace {@code assets/necromancy/textures/gui/jei/sewing.png}.
     * Size {@value #JEI_SEWING_BG_WIDTH}×{@value #JEI_SEWING_BG_HEIGHT} pixels (slots are drawn at fixed offsets on top).
     */
    public static final ResourceLocation TEXTURES_GUI_JEI_SEWING = rl("textures/gui/jei/sewing.png");
    public static final int JEI_SEWING_BG_WIDTH  = 132;
    public static final int JEI_SEWING_BG_HEIGHT = 88;

    /**
     * JEI handbook / info pane — replace {@code assets/necromancy/textures/gui/jei/guide.png}.
     * Size {@value #JEI_GUIDE_BG_WIDTH}×{@value #JEI_GUIDE_BG_HEIGHT} pixels.
     */
    public static final ResourceLocation TEXTURES_GUI_JEI_GUIDE = rl("textures/gui/jei/guide.png");
    public static final int JEI_GUIDE_BG_WIDTH  = 220;
    public static final int JEI_GUIDE_BG_HEIGHT = 136;

    public static final ResourceLocation TEXTURE_PARTICLES         = rl("textures/particles.png");

    public static final ResourceLocation TEXTURE_ENTITY_VILLAGER   = rl("textures/entities/villagernecro.png");
    public static final ResourceLocation TEXTURE_ENTITY_NIGHT      = rl("textures/entities/nightcrawler.png");
    public static final ResourceLocation TEXTURE_ENTITY_TEDDY      = rl("textures/entities/teddy.png");
    public static final ResourceLocation TEXTURE_ENTITY_ISAAC      = rl("textures/entities/isaac.png");
    public static final ResourceLocation TEXTURE_ENTITY_ISAAC_BLOOD= rl("textures/entities/isaacblood.png");

    public static final ResourceLocation TEXTURE_SADDLE_SPIDER     = rl("textures/entities/spidersaddle.png");
    public static final ResourceLocation TEXTURE_SADDLE_COW        = rl("textures/entities/cowsaddle.png");
    public static final ResourceLocation TEXTURE_SADDLE_SQUID      = rl("textures/entities/squidsaddle.png");

    public static final ResourceLocation TEXTURE_MODEL_ALTAR       = rl("textures/models/altartexture.png");
    public static final ResourceLocation TEXTURE_MODEL_SEWING      = rl("textures/models/sewingtexture.png");
    public static final ResourceLocation TEXTURE_MODEL_NECRONOMICON= rl("textures/models/necronomicon.png");
    public static final ResourceLocation TEXTURE_MODEL_SCYTHE      = rl("textures/models/scythe.png");
    public static final ResourceLocation TEXTURE_MODEL_SCYTHE_BONE = rl("textures/models/scythebone.png");

    public static final ResourceLocation TEXTURE_ARMOR_ISAAC       = rl("textures/models/armor/isaacarmor.png");

    /** Sound event ids registered in {@code sounds.json}. */
    public static final ResourceLocation SOUND_NIGHTCRAWLER_HOWL   = rl("nightcrawler.howl");
    public static final ResourceLocation SOUND_NIGHTCRAWLER_SCREAM = rl("nightcrawler.scream");
    public static final ResourceLocation SOUND_SPAWN               = rl("spawn");
    public static final ResourceLocation SOUND_TEAR                = rl("tear");
}
