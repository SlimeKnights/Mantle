package mantle.compat.plugins

import mantle.compat.lib.CompatRepo._

/**
 * Debug info plugin
 *
 * @author Sunstrike <sunstrike@azurenode.net>
 */
object PluginDebug extends ICompatPlugin {

  def providedMod: String = "Mantle-Compat"

  def accept() {
    log("Accepted by manager.")
  }

  def preInit() {
    log("Reached preinit.")
  }

  def init() {
    log("Reached init.")
  }

  def postInit() {
    log("Reached postinit.")
  }

  private def log(msg:String) {
    logger.info(s"[PluginDebug] $msg")
  }

}
