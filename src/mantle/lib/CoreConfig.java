package mantle.lib;

import net.minecraftforge.common.config.Configuration;
import static mantle.lib.CoreRepo.*;

/**
 * Mantle configuration handler
 *
 * Stores configuration data for Mantle, and handles save/load.
 *
 * @author Sunstrike <sun@sunstrike.io>
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

        debug_enableConsole = config.get("DebugHelpers", "enableConsole", debug_enableConsole).getBoolean(debug_enableConsole);
        debug_enableChat = config.get("DebugHelpers", "enableChat", debug_enableChat).getBoolean(debug_enableChat);
        //check for debugging overrides in system environment
        checkSysOverrides();
        config.save();
        logger.info("Configuration load completed.");

    }

    /**
     * Checks for MANTLE_DBGCONSOLE/CHAT config overrides in system environment for compile time testing, and other dev work.
     **/
    private static void checkSysOverrides ()
    {
        if (System.getenv("MANTLE_DBGCONSOLE") != null)
        {
            debug_enableConsole = true;
            logger.info("Mantle Console debugging override enabled via system properties.");
        }
        if (System.getenv("MANTLE_DBGCHAT") != null)
        {
            debug_enableChat = true;
            logger.info("Mantle Chat debugging override enabled via system properties.");
        }
    }

    // BEGIN CONFIG VARS
    // Env Checks
    public static boolean silenceEnvChecks = false;

    // Debug Helpers
    public static boolean debug_enableConsole = false;
    public static boolean debug_enableChat = false;

}
