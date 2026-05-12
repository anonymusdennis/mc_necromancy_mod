package com.sirolf2009.necromancy.lib;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.io.File;

public class ConfigurationNecromancy
{
    public static int rarityIsaacs = 300;
    public static int rarityNightCrawlers = 200;
    public static int OrgansID = 5300;

    public static void initProperties(FMLPreInitializationEvent event)
    {
        Configuration config = new Configuration(new File(event.getModConfigurationDirectory(), "necromancy.cfg"));
        config.load();
        rarityIsaacs = config.get("general", "rarityIsaacs", 300,
                "How rare Isaacs are to spawn. 1-in-N chance per zombie/skeleton spawn.").getInt();
        rarityNightCrawlers = config.get("general", "rarityNightCrawlers", 200,
                "How rare NightCrawlers are to spawn. 1-in-N chance per zombie/skeleton spawn.").getInt();
        OrgansID = config.get("general", "OrgansID", 5300,
                "Item damage value used for the Organs item sub-type discriminator.").getInt();
        if (config.hasChanged())
        {
            config.save();
        }
    }
}
