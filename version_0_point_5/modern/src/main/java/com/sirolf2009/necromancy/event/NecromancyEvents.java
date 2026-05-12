package com.sirolf2009.necromancy.event;

import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * Aggregate "game-bus" event registration.  Empty for now -- individual
 * handlers (replace zombies/skeletons, organ drops, fill-bucket pickup) live
 * inside this package and are added in {@code register()} once they exist.
 */
public final class NecromancyEvents {
    private NecromancyEvents() {}
    public static void register() {
        NeoForge.EVENT_BUS.register(ForgeEventHandler.class);
        NeoForge.EVENT_BUS.register(NecroVillagerTrades.class);
        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent ev) ->
            com.sirolf2009.necromancy.command.CommandMinion.register(ev));
    }
}
