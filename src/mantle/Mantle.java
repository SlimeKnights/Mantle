package mantle;

import java.util.logging.Logger;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.Mod.EventHandler;
import net.minecraftforge.common.Configuration;
import mantle.lib.CoreRepo;
import mantle.lib.CoreConfig;
import mantle.internal.EnvironmentChecks;

/**
 * Mantle
 *
 * Central mod object for Mantle
 *
 * @author Sunstrike <sunstrike@azurenode.net>
 */
@Mod(modid = CoreRepo.modId, name = CoreRepo.modName, version = CoreRepo.modVersion, dependencies = "required-after:Forge@[8.9,)")
@NetworkMod(clientSideRequired = true, serverSideRequired = false)
public class Mantle {
    
    public static final Logger logger = Logger.getLogger(CoreRepo.modName);


  /**
   * FML preinitialisation handler
   *
   * This is where we load our configs and related data, preparing for main load.
   *
   * @param evt The FMLPreInitializationEvent from FML
   */
  @EventHandler
  public void preInit (FMLPreInitializationEvent event){
    logger.setParent(FMLCommonHandler.instance().getFMLLogger());

    CoreConfig.loadConfiguration(new Configuration(event.getSuggestedConfigurationFile()));
    
    logger.info("Mantle (" + CoreRepo.modVersion + ") -- Preparing for launch.");
    logger.info("Entering preinitialization phase.");

    EnvironmentChecks.verifyEnvironmentSanity();
  }

  /**
   * FML preinitialisation handler
   *
   * This is where we handle basic loading and populating any missing data in the Repo
   *
   * @param evt The FMLInitializationEvent from FML
   */
  @EventHandler
  public void Init (FMLPreInitializationEvent event){
    logger.info("Entering initialization phase.");
  }

  /**
   * FML preinitialisation handler
   *
   * Final chance for cleanup before main game launch
   *
   * @param evt The FMLPostInitializationEvent from FML
   */
  @EventHandler
  public void postInit (FMLPreInitializationEvent event){
    logger.info("Entering postinitialization phase.");
  }

}
