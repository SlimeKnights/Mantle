package mantle.lib;

import static mantle.lib.CoreRepo.logger;
import static mantle.lib.CoreRepo.modId;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;

import mantle.books.external.ZipLoader;
import net.minecraftforge.common.config.Configuration;

/**
 * Mantle configuration handler
 *
 * Stores configuration data for Mantle, and handles save/load.
 *
 * @author Sunstrike <sun@sunstrike.io>
 */
public class CoreConfig
{
    static Configuration config;
    public static File configFolder;

    /**
     * Loads state from a Forge configuration object, or saves a new file if it doesn't already exist.
     *
     * @param config Configuration object (usually from a location given by FML in preinit)
     */
    public static void loadConfiguration (File mainConfigFolder)
    {
        logger.info("Loading configuration from disk.");
        try
        {
            configFolder = new File(mainConfigFolder.getCanonicalPath(), "SlimeKnights");
            if (!configFolder.exists())
                configFolder.mkdirs();
        }
        catch (IOException e)
        {
            logger.error("Error creating Mantle's Config File: " + e.getMessage());
        }

        config = new Configuration(new File(configFolder, (modId + ".cfg")));
        config.load();

        silenceEnvChecks = config.get("Environment", "unsupportedLogging", silenceEnvChecks).getBoolean(silenceEnvChecks);

        debug_enableConsole = config.get("DebugHelpers", "enableConsole", debug_enableConsole).getBoolean(debug_enableConsole);
        debug_enableChat = config.get("DebugHelpers", "enableChat", debug_enableChat).getBoolean(debug_enableChat);
        //check for debugging overrides in system environment
        dumpBiomeIDs = config.get("DebugHelpers", "Dump BIOME ID's in log", dumpBiomeIDs).getBoolean(dumpBiomeIDs);
        dumpPotionIDs = config.get("DebugHelpers", "Dump POTION ID's in log", dumpPotionIDs).getBoolean(dumpPotionIDs);
        dumpEnchantIDs = config.get("DebugHelpers", "Dump ENCHANT ID's in log", dumpEnchantIDs).getBoolean(dumpEnchantIDs);

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

    public static void loadBookLocations ()
    {
        for (File f : configFolder.listFiles())
        {
            if(f.isFile() && FilenameUtils.getExtension(f.getAbsolutePath()).equalsIgnoreCase("zip"))
                ZipLoader.loadZip(f);
        }
    }

    // BEGIN CONFIG VARS
    // Env Checks
    public static boolean silenceEnvChecks = false;

    // Debug Helpers
    public static boolean debug_enableConsole = false;
    public static boolean debug_enableChat = false;
    public static boolean dumpBiomeIDs = false;
    public static boolean dumpPotionIDs = false;
    public static boolean dumpEnchantIDs = false;


}
