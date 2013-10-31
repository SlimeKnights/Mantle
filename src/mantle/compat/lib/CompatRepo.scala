package mantle.compat.lib

import java.util.logging.Logger

/**
 * Mantle-Compat object repository
 *
 * Storage area for objects accessible throughout Compat, such as loggers
 *
 * @author Sunstrike <sunstrike@azurenode.net>
 */
object CompatRepo {

  final val modId = "Mantle-Compat"
  final val modName = "Mantle Compat"
  final val modVersion = "@VERSION@"

  val logger = Logger.getLogger(modName)

  // Debug flags
  val debugCompatPlugins = System.getenv("MANTLE_COMPAT") != null

}
