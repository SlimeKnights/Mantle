package mantle.lib;

import net.minecraftforge.common.Configuration;
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

        // Also consult MANTLE_DBGCONSOLE/CHAT for override purposes.
        debug_enableConsole = config.get("DebugHelpers", "enableConsole", debug_enableConsole).getBoolean(debug_enableConsole);
        if (System.getenv("MANTLE_DBGCONSOLE") != null) debug_enableConsole = true;
        debug_enableChat = config.get("DebugHelpers", "enableChat", debug_enableChat).getBoolean(debug_enableChat);
        if (System.getenv("MANTLE_DBGCHAT") != null) debug_enableChat = true;

        config.save();
        logger.info("Configuration load completed.");
    }

    // BEGIN CONFIG VARS
    // Env Checks
    public static boolean silenceEnvChecks = false;

    // Debug Helpers
    public static boolean debug_enableConsole = false;
    public static boolean debug_enableChat = false;

}
