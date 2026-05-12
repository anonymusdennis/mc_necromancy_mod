package com.sirolf2009.necromancy.block;

import net.minecraft.world.level.block.Block;

/**
 * The "altar building block" -- one of the two stone blocks that make up the
 * altar's base together with {@link BlockAltar}.
 *
 * <p>Cosmetic only in the legacy mod; we keep the same behaviour (functions as
 * a normal cobblestone-coloured block).
 */
public class BlockAltarBlock extends Block {
    public BlockAltarBlock(Properties props) { super(props); }
}
