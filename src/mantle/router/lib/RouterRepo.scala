package mantle.router.lib

import java.util.logging.Logger

/**
  * Mantle-Router object repository
  *
  * Storage area for objects accessible throughout Router, such as loggers
  *
  * @author Sunstrike <sunstrike@azurenode.net>
  */
object RouterRepo {

   final val modId = "Mantle-Router"
   final val modName = "Mantle Router"
   final val modVersion = "@VERSION@"

   val logger = Logger.getLogger(modName)

 }
