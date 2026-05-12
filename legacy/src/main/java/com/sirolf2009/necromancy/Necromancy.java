package com.sirolf2009.necromancy;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.server.dedicated.PropertyManager;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.VillagerRegistry;

import atomicstryker.necromancy.network.NetworkHelper;
import atomicstryker.necromancy.network.TearShotPacket;

import com.sirolf2009.necromancy.block.RegistryBlocksNecromancy;
import com.sirolf2009.necromancy.client.gui.GuiHandler;
import com.sirolf2009.necromancy.command.CommandMinion;
import com.sirolf2009.necromancy.core.handler.ForgeEventHandler;
import com.sirolf2009.necromancy.core.proxy.CommonProxy;
import com.sirolf2009.necromancy.craftingmanager.CraftingManagerSewing;
import com.sirolf2009.necromancy.creativetab.CreativeTabNecro;
import com.sirolf2009.necromancy.entity.RegistryNecromancyEntities;
import com.sirolf2009.necromancy.generation.VillageCreationHandler;
import com.sirolf2009.necromancy.generation.WorldGenerator;
import com.sirolf2009.necromancy.generation.villagecomponent.ComponentVillageCemetery;
import com.sirolf2009.necromancy.item.ItemNecroSkull;
import com.sirolf2009.necromancy.item.RegistryNecromancyItems;
import com.sirolf2009.necromancy.lib.ConfigurationNecromancy;
import com.sirolf2009.necromancy.lib.ReferenceNecromancy;

@Mod(modid = ReferenceNecromancy.MOD_ID, name = ReferenceNecromancy.MOD_NAME, version = ReferenceNecromancy.MOD_VERSION,
        dependencies = "required-after:Forge")
public class Necromancy
{
    public static final CreativeTabs tabNecromancy = new CreativeTabNecro(ReferenceNecromancy.MOD_NAME, 1);
    public static final CreativeTabs tabNecromancyBodyParts = new CreativeTabNecro("BodyParts", 2);

    public static final List<String> specialFolk = new ArrayList<>();

    public int maxSpawn = -1;

    public static Logger loggerNecromancy;

    public static final GuiHandler guiHandler = new GuiHandler();
    public static final ForgeEventHandler eventHandler = new ForgeEventHandler();
    public static final VillageCreationHandler villageHandler = new VillageCreationHandler();

    public CraftingManagerSewing sewingRecipeHandler;

    @SidedProxy(clientSide = ReferenceNecromancy.CLIENT_PROXY, serverSide = ReferenceNecromancy.COMMON_PROXY)
    public static CommonProxy proxy;

    @Instance(ReferenceNecromancy.MOD_ID)
    public static Necromancy instance;

    public NetworkHelper networkHelper;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        loggerNecromancy = Logger.getLogger(ReferenceNecromancy.MOD_ID);

        ConfigurationNecromancy.initProperties(event);

        networkHelper = new NetworkHelper(ReferenceNecromancy.MOD_NAME, TearShotPacket.class);

        NetworkRegistry.INSTANCE.registerGuiHandler(this, guiHandler);

        FMLCommonHandler.instance().bus().register(eventHandler);
        MinecraftForge.EVENT_BUS.register(eventHandler);

        proxy.preInit();

        specialFolk.add("AtomicStryker");

        try
        {
            URL url = new URL("https://dl.dropboxusercontent.com/u/50553915/necromancy/specialFolk.txt");
            Scanner s = new Scanner(url.openStream());
            while (s.hasNext())
            {
                specialFolk.add(s.nextLine());
            }
            s.close();
        }
        catch (IOException e)
        {
            System.err.println("Necromancy: not connected to the internet, special scythes are de-activated");
        }

        RegistryBlocksNecromancy.initBlocks();
        RegistryNecromancyItems.initItems();
        RegistryNecromancyEntities.initEntities();

        MapGenStructureIO.registerStructureComponent(ComponentVillageCemetery.class, "NeViCem");
        VillagerRegistry.instance().registerVillageCreationHandler(villageHandler);
        GameRegistry.registerWorldGenerator(new WorldGenerator(), 5);
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        proxy.init();
        RegistryBlocksNecromancy.initRecipes();
        RegistryNecromancyItems.initRecipes();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        ItemNecroSkull.initSkulls();
        sewingRecipeHandler = new CraftingManagerSewing();
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event)
    {
        event.registerServerCommand(new CommandMinion());
    }

    @EventHandler
    public void serverStarted(FMLServerStartedEvent event)
    {
        if (new File("server.properties").exists())
        {
            PropertyManager manager = new PropertyManager(new File("server.properties"), null);
            maxSpawn = manager.getIntProperty("max_minion_spawn", -1);
        }
    }
}
