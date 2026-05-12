package com.sirolf2009.necromancy.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

import com.sirolf2009.necromancy.entity.EntityMinion;

import java.util.List;

public class CommandMinion extends CommandBase
{
    @Override
    public String getName() { return "minion"; }

    @Override
    public String getUsage(ICommandSender sender) { return "/minion <aggressive|passive|enemy|ally> [playerName]"; }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args)
    {
        if (!(sender instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) sender;
        NBTTagCompound nbt = player.getEntityData();

        if (args.length == 0)
        {
            sender.sendMessage(new TextComponentString("Usage: " + getUsage(sender)));
            return;
        }

        switch (args[0].toLowerCase())
        {
        case "aggressive":
            nbt.setBoolean("aggressive", true);
            sender.sendMessage(new TextComponentString("Your minions are now aggressive to strangers."));
            break;
        case "passive":
            nbt.setBoolean("aggressive", false);
            sender.sendMessage(new TextComponentString("Your minions are now passive."));
            break;
        case "enemy":
            if (args.length >= 2) nbt.setString(args[1], "enemy");
            break;
        case "ally":
            if (args.length >= 2) nbt.setString(args[1], "ally");
            break;
        case "kill":
            List<EntityMinion> minions = player.world.getEntities(EntityMinion.class,
                    m -> m.getOwner() != null && m.getOwner().equals(player));
            minions.forEach(m -> m.setDead());
            sender.sendMessage(new TextComponentString("All minions dismissed."));
            break;
        default:
            sender.sendMessage(new TextComponentString("Unknown command: " + args[0]));
        }
    }
}
