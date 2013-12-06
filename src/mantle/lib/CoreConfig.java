package mantle.lib;

import net.minecraftforge.common.Configuration;
import mantle.Mantle;
import mantle.lib.CoreRepo.*;

/**
 * Mantle configuration handler
 *
 * Stores configuration data for Mantle, and handles save/load.
 *
 * @author Sunstrike <sunstrike@azurenode.net>
 */
public class CoreConfig
{

    /**
     * Loads state from a Forge configuration object, or saves a new file if it doesn't already exist.
     *
     * @param config Configuration object (usually from a location given by FML in preinit)
     */
    public static void loadConfiguration (Configuration config)
    {
        Mantle.logger.info("Loading configuration from disk.");
        config.load();

        // TODO: Config vars get/set here

        config.save();
        Mantle.logger.info("Configuration load completed.");
    }

}
