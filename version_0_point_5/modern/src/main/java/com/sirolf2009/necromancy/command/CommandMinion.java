package com.sirolf2009.necromancy.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.sirolf2009.necromancy.api.BodyPartLocation;
import com.sirolf2009.necromancy.api.NecroEntityBase;
import com.sirolf2009.necromancy.api.NecroEntityRegistry;
import com.sirolf2009.necromancy.entity.EntityMinion;
import com.sirolf2009.necromancy.entity.NecromancyEntities;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Random;

/**
 * Brigadier port of the legacy {@code /minion} chat command.
 *
 * <p>Spawns a minion in front of the executing player.  Supports two modes:
 * <ul>
 *     <li>{@code /minion} -- random adapter for every body part,</li>
 *     <li>{@code /minion <mob>} -- all five body parts taken from a single
 *         registered adapter (e.g. {@code /minion Cow}).</li>
 * </ul>
 */
public final class CommandMinion {

    private static final Random RNG = new Random();

    private CommandMinion() {}

    public static void register(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    public static void register(CommandDispatcher<CommandSourceStack> disp) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("minion")
            .requires(s -> s.hasPermission(2))
            .executes(ctx -> spawn(ctx, null));

        for (NecroEntityBase base : NecroEntityRegistry.registeredEntities.values()) {
            final String name = base.mobName;
            root.then(Commands.literal(name).executes(ctx -> spawn(ctx, name)));
        }
        disp.register(root);
    }

    private static boolean has(NecroEntityBase base, BodyPartLocation loc) {
        return switch (loc) {
            case Head     -> base.hasHead;
            case Torso    -> base.hasTorso;
            case ArmLeft, ArmRight -> base.hasArms;
            case Legs     -> base.hasLegs;
        };
    }

    private static int spawn(CommandContext<CommandSourceStack> ctx, String forced) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        EntityMinion minion = NecromancyEntities.MINION.get().create(player.serverLevel());
        if (minion == null) return 0;

        var registry = NecroEntityRegistry.registeredEntities;
        if (registry.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal("No NecroEntity adapters are registered."));
            return 0;
        }
        NecroEntityBase[] all = registry.values().toArray(new NecroEntityBase[0]);

        for (BodyPartLocation loc : BodyPartLocation.values()) {
            String mob = (forced != null) ? forced : all[RNG.nextInt(all.length)].mobName;
            NecroEntityBase base = NecroEntityRegistry.get(mob);
            if (base != null && has(base, loc)) {
                minion.setBodyPartName(loc, base.mobName);
                base.setAttributes(minion, loc);
            }
        }

        Vec3 look = player.getLookAngle();
        minion.moveTo(player.getX() + look.x * 1.5, player.getY(), player.getZ() + look.z * 1.5,
                      player.getYRot(), 0F);
        minion.setOwnerUUID(player.getUUID());
        minion.setTame(true, false);
        player.serverLevel().addFreshEntity(minion);
        ctx.getSource().sendSuccess(() -> Component.literal("Spawned minion."), true);
        return 1;
    }
}
