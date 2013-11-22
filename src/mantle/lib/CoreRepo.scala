package mantle.lib

import java.util.logging.Logger

/**
 * Mantle object repository
 *
 * Storage area for objects accessible throughout Mantle, such as loggers
 *
 * @author Sunstrike <sunstrike@azurenode.net>
 */
object CoreRepo {

  final val modId = "Mantle"
  final val modName = "Mantle"
  final val modVersion = "@VERSION@"

  val logger = Logger.getLogger(modName)

}
