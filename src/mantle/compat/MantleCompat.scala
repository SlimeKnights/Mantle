package mantle.compat

import cpw.mods.fml.common.{FMLCommonHandler, Mod}
import cpw.mods.fml.common.event.{FMLPostInitializationEvent, FMLInitializationEvent, FMLPreInitializationEvent}
import cpw.mods.fml.common.Mod.EventHandler

import mantle.compat.lib.CompatRepo._
import mantle.compat.plugins.{PluginDebug, PluginManager}
import mantle.compat.lib.CompatConfig
import net.minecraftforge.common.Configuration

/**
 * Mantle-Compat
 *
 * Central mod object for Mantle (Compat Module)
 *
 * @author Sunstrike <sunstrike@azurenode.net>
 */
@Mod(modid = modId, name = modName, version = modName, dependencies = "required-after:Mantle-Core; required-after:Mantle-Router", modLanguage = "scala")
object MantleCompat {

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

    CompatConfig.loadConfiguration(new Configuration(evt.getSuggestedConfigurationFile))

    if (debugCompatPlugins) PluginManager.registerPluginCandidate(PluginDebug)
    PluginManager.preInitPlugins()
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
    PluginManager.initPlugins()
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
    PluginManager.postInitPlugins()
  }

}
