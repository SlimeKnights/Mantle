package mantle.router

import cpw.mods.fml.common.{FMLCommonHandler, Mod}
import cpw.mods.fml.common.event.{FMLInterModComms, FMLPostInitializationEvent, FMLInitializationEvent, FMLPreInitializationEvent}
import cpw.mods.fml.common.Mod.EventHandler

import mantle.router.lib.RouterRepo._
import mantle.router.imc.{UnitDebugLogger, IMCHandler}
import cpw.mods.fml.common.event.FMLInterModComms.IMCEvent

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
    logger.info("Router prepared for IMC message receipt.")
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

  }

  /**
   * IMC event handler
   *
   * Called shortly after init to handle any IMC messages we've been sent.
   *
   * @param evt An IMCEvent.
   */
  @EventHandler
  def retrieveIMC(evt:IMCEvent) {
    registerStandardIMCUnits()
    IMCHandler.handle(evt)
  }

  /**
   * Registrar for built-in Router pipeline objects
   */
  private def registerStandardIMCUnits() {
    if (System.getenv("MANTLE_IMC") != null) IMCHandler.registerPipelineUnit(UnitDebugLogger) // Logs all IMC to console IF MANTLE_IMC is defined in env
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
