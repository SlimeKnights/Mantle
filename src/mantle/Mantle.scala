package mantle

import cpw.mods.fml.common.{FMLCommonHandler, Mod}
import cpw.mods.fml.common.network.NetworkMod
import cpw.mods.fml.common.event.{FMLPostInitializationEvent, FMLInitializationEvent, FMLPreInitializationEvent}
import cpw.mods.fml.common.Mod.EventHandler
import net.minecraftforge.common.Configuration

import mantle.lib.CoreRepo._
import mantle.lib.CoreConfig
import mantle.internal.EnvironmentChecks

/**
 * Mantle
 *
 * Central mod object for Mantle
 *
 * @author Sunstrike <sunstrike@azurenode.net>
 */
@Mod(modid = modId, name = modName, version = modVersion, modLanguage = "scala")
@NetworkMod(clientSideRequired = true, serverSideRequired = false)
object Mantle {

  /**
   * FML preinitialisation handler
   *
   * This is where we load our configs and related data, preparing for main load.
   *
   * @param evt The FMLPreInitializationEvent from FML
   */
  @EventHandler
  def preInit(evt:FMLPreInitializationEvent) {
    logger.setParent(FMLCommonHandler.instance().getFMLLogger)

    CoreConfig.loadConfiguration(new Configuration(evt.getSuggestedConfigurationFile))
    
    logger.info("Mantle (" + modVersion + ") -- Preparing for launch.")
    logger.info("Entering preinitialization phase.")

    EnvironmentChecks.examineEnvironment()
  }

  /**
   * FML preinitialisation handler
   *
   * This is where we handle basic loading and populating any missing data in the Repo
   *
   * @param evt The FMLInitializationEvent from FML
   */
  @EventHandler
  def init(evt:FMLInitializationEvent) {
    logger.info("Entering initialization phase.")
  }

  /**
   * FML preinitialisation handler
   *
   * Final chance for cleanup before main game launch
   *
   * @param evt The FMLPostInitializationEvent from FML
   */
  @EventHandler
  def postInit(evt:FMLPostInitializationEvent) {
    logger.info("Entering postinitialization phase.")
  }

}
