package mantle.compat.plugins.builtin

import mantle.compat.plugins.ICompatPlugin
import mantle.router.routing.RouterCoordinator
import mantle.router.routing.builtin.UnitMystcraft

/**
  * Mystcraft compatibility plugin
  *
  * @author Sunstrike <sunstrike@azurenode.net>
  */
object PluginMystcraft extends ICompatPlugin {

   def providedMod: String = "Mystcraft"

   def accept() {
     RouterCoordinator.registerPipelineUnit(UnitMystcraft) // Register AE pipeline object
   }

   def preInit() {

   }

   def init() {

   }

   def postInit() {

   }

 }
