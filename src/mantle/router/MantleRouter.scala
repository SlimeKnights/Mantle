package mantle.router

import cpw.mods.fml.common.{FMLCommonHandler, Mod}
import cpw.mods.fml.common.event.{FMLPostInitializationEvent, FMLInitializationEvent, FMLPreInitializationEvent}
import cpw.mods.fml.common.Mod.EventHandler

import mantle.router.lib.RouterRepo._

/**
 * Mantle-Router
 *
 * Central mod object for Mantle (Router Module)
 *
 * @author Sunstrike <sunstrike@azurenode.net>
 */
@Mod(modid = modId, name = modName, version = modName, modLanguage = "scala")
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
    logger.info("Router prepared for IMC message receipt.")
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

  }

}
