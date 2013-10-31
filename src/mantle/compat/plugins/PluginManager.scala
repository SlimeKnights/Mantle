package mantle.compat.plugins

import scala.collection.mutable.MutableList
import cpw.mods.fml.common.Loader

import mantle.compat.lib.CompatRepo._

/**
 * Mantle-Compat Plugin Manager
 *
 * Coordinates all compat plugins in the Mantle-Compat framework.
 *
 * @author Sunstrike <sunstrike@azurenode.net>
 */
object PluginManager {

  private val activePlugins = MutableList[ICompatPlugin]()

  /**
   * Register a candidate compat plugin
   *
   * Tests if the candidate's provided mod is actually running in the environment before adding to the active plugins list.
   *
   * @param candidate The plugin to attempt to load.
   * @return True if plugin was loaded, False if plugin was rejected.
   */
  def registerPluginCandidate(candidate:ICompatPlugin):Boolean = {
    if (!Loader.isModLoaded(candidate.providedMod)) {
      debug(s"Rejecting: $candidate")
      return false
    }

    debug(s"Accepting: $candidate")
    candidate.accept()
    activePlugins += candidate
    true
  }

  /**
   * Called during Compat preinit
   *
   * @note For internal use only!
   */
  def preInitPlugins() {
    debug(s"Performing preinit (plugins = $activePlugins)")
    activePlugins map { x => x.preInit() }
  }

  /**
   * Called during Compat init
   *
   * @note For internal use only!
   */
  def initPlugins() {
    debug(s"Performing init (plugins = $activePlugins)")
    activePlugins map { x => x.init() }
  }

  /**
   * Called during Compat postinit
   *
   * @note For internal use only!
   */
  def postInitPlugins() {
    debug(s"Performing postinit (plugins = $activePlugins)")
    activePlugins map { x => x.postInit() }
  }

  /**
   * Internal logging helper
   *
   * Prepends appropriate tag and only logs if debug flag MANTLE_COMPAT is active.
   *
   * @param msg String to log.
   */
  private def debug(msg:String) {
    if (debugCompatPlugins) logger.info(s"[PluginManager] $msg")
  }

}
