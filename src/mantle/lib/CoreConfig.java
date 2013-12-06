package mantle.lib;

import net.minecraftforge.common.Configuration;
import static mantle.lib.CoreRepo.*;

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
        logger.info("Loading configuration from disk.");
        config.load();

        silenceEnvChecks = config.get("Environment", "unsupportedLogging", silenceEnvChecks).getBoolean(silenceEnvChecks);

        config.save();
        logger.info("Configuration load completed.");
    }

    // BEGIN CONFIG VARS
    public static boolean silenceEnvChecks = false;

}
