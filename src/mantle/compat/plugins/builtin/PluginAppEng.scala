package mantle.compat.plugins.builtin

import mantle.compat.plugins.ICompatPlugin
import mantle.router.routing.RouterCoordinator
import mantle.router.routing.builtin.UnitAppEng

/**
 * Applied Energistics compatibility plugin
 *
 * @author Sunstrike <sunstrike@azurenode.net>
 */
object PluginAppEng extends ICompatPlugin {

  def providedMod: String = "AppliedEnergistics"

  def accept() {
    RouterCoordinator.registerPipelineUnit(UnitAppEng) // Register AE pipeline object
  }

  def preInit() {

  }

  def init() {

  }

  def postInit() {

  }

}
