package com.sirolf2009.necromancy.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * Stub hooks for future dissection / scalpel tooling (ideas.md lines&nbsp;77–85).
 */
public final class CommandNecromancyDissectStub {

    private CommandNecromancyDissectStub() {}

    public static void register(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> dissect = Commands.literal("necromancy_dissect_stub")
            .requires(s -> s.hasPermission(2))
            .executes(ctx -> {
                ctx.getSource().sendSuccess(() -> Component.translatable("commands.necromancy.dissect.stub"), false);
                return 1;
            });
        dispatcher.register(dissect);
    }
}
