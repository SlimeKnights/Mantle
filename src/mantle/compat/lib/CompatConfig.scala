package mantle.compat.lib

import net.minecraftforge.common.Configuration

import mantle.core.lib.CoreRepo._

/**
 * Mantle-Compat configuration handler
 *
 * Stores configuration data for Mantle-Compat, and handles save/load.
 *
 * @author Sunstrike <sunstrike@azurenode.net>
 */
object CompatConfig {

  /**
   * Loads state from a Forge configuration object, or saves a new file if it doesn't already exist.
   *
   * @param config Configuration object (usually from a location given by FML in preinit)
   */
  def loadConfiguration(config:Configuration) {
    logger.info("Loading configuration from disk.")
    config.load()

    // TODO: Config vars get/set here

    config.save()
    logger.info("Configuration load completed.")
  }

}
