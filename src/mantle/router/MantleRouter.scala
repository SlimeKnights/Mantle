package mantle.router

import cpw.mods.fml.common.{FMLCommonHandler, Mod}
import cpw.mods.fml.common.event.{FMLPostInitializationEvent, FMLInitializationEvent, FMLPreInitializationEvent}
import cpw.mods.fml.common.Mod.EventHandler

import mantle.router.lib.RouterRepo._
import mantle.router.routing.{UnitDebugLogger, RouterCoordinator}
import mantle.router.lib.RouterConfig
import net.minecraftforge.common.Configuration

/**
 * Mantle-Router
 *
 * Central mod object for Mantle (Router Module)
 *
 * @author Sunstrike <sunstrike@azurenode.net>
 */
@Mod(modid = modId, name = modName, version = modName, dependencies = "required-after:Mantle-Core", modLanguage = "scala")
object MantleRouter {

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

    RouterConfig.loadConfiguration(new Configuration(evt.getSuggestedConfigurationFile))

    registerStandardUnits()
  }

  /**
   * FML preinitialisation handler
   *
   * This is where we handle basic loading and populating any missing data in the Repo.
   * The Router also processes events here.
   *
   * @param evt The FMLInitializationEvent from FML
   */
  @EventHandler
  def init(evt:FMLInitializationEvent) {
    registerStandardUnits()
    RouterCoordinator.processQueue()
  }

  /**
   * Registrar for built-in Router pipeline objects
   */
  private def registerStandardUnits() {
    if (debugMessageSys) RouterCoordinator.registerPipelineUnit(UnitDebugLogger)
  }

  /**
   * FML preinitialisation handler
   *
   * Final chance for cleanup before main game launch.
   * The Router also processes events here.
   *
   * @param evt The FMLPostInitializationEvent from FML
   */
  @EventHandler
  def postInit(evt:FMLPostInitializationEvent) {
    RouterCoordinator.processQueue()
  }

}
