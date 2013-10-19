package mantle;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import mantle.internal.Config;
import mantle.network.PacketHandler;
import net.minecraftforge.common.Configuration;

import java.util.logging.Logger;

@Mod(modid = "Mantle", name = "Mantle", version = "@VERSION@", modLanguage = "java")
@NetworkMod(channels = "Mantle", packetHandler = PacketHandler.class)
public class Mantle {

    public static Logger logger = Logger.getLogger("Mantle");

    public Mantle() {
        logger.setParent(FMLCommonHandler.instance().getFMLLogger());
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent evt) {
        Config.loadConfig(new Configuration(evt.getSuggestedConfigurationFile()));
        if (Loader.isModLoaded("gregtech_addon")) logger.severe("Gregtech is currently loaded. Support will not be provided for any bugs encountered in this state.");
        logger.info("Preinitialisation completed.");
    }

    @EventHandler
    public void init(FMLInitializationEvent evt) {
        logger.info("Initialisation completed.");
    }

    public void postInit(FMLPostInitializationEvent evt) {
        logger.info("Postinitialisation completed.");
    }

}
