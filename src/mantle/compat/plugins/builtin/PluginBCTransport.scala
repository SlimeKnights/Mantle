package mantle.compat.plugins.builtin

import mantle.compat.plugins.ICompatPlugin
import mantle.router.routing.RouterCoordinator
import mantle.router.routing.builtin.UnitBCTransport

/**
 * Buildcraft Transport plugin
 *
 * @author Sunstrike <sunstrike@azurenode.net>
 */
object PluginBCTransport extends ICompatPlugin {

  def providedMod = "BuildCraft|Transport"

  def accept() {
    RouterCoordinator.registerPipelineUnit(UnitBCTransport)
  }

  def preInit() {

  }

  def init() {

  }

  def postInit() {

  }
}
