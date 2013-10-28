package mantle.core.lib

import java.util.logging.Logger

/**
 * Mantle-Core object repository
 *
 * Storage area for objects accessible throughout Core, such as loggers
 *
 * @author Sunstrike <sunstrike@azurenode.net>
 */
object CoreRepo {

  final val modId = "Mantle-Core"
  final val modName = "Mantle Core"
  final val modVersion = "@VERSION@"

  val logger = Logger.getLogger(modName)

}
