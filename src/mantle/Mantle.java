package mantle;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import net.minecraftforge.common.Configuration;
import static mantle.lib.CoreRepo.*;
import mantle.common.MProxyCommon;
import mantle.lib.CoreConfig;
import mantle.lib.environment.EnvironmentChecks;

/**
 * Mantle
 *
 * Central mod object for Mantle
 *
 * @author Sunstrike <sun@sunstrike.io>
 */
@Mod(modid = modId, name = modName, version = modVersion, dependencies = "required-after:Forge@[8.9,)")
@NetworkMod(clientSideRequired = true, serverSideRequired = false)
public class Mantle
{
    /* Instance of this mod, used for grabbing prototype fields */
    @Instance("Mantle")
    public static Mantle instance;
    /* Proxies for sides, used for graphics processing */
    @SidedProxy(clientSide = "Mantle.client.MProxyClient", serverSide = "Mantle.common.MProxyCommon")
    public static MProxyCommon proxy;

    /**
     * FML preinitialisation handler
     *
     * This is where we load our configs and related data, preparing for main load.
     *
     * @param evt The FMLPreInitializationEvent from FML
     */
    @EventHandler
    public void preInit (FMLPreInitializationEvent evt)
    {
        logger.setParent(FMLCommonHandler.instance().getFMLLogger());

        CoreConfig.loadConfiguration(new Configuration(evt.getSuggestedConfigurationFile()));

        logger.info("Mantle (" + modVersion + ") -- Preparing for launch.");
        logger.info("Entering preinitialization phase.");

        EnvironmentChecks.verifyEnvironmentSanity();
    }

    /**
     * FML preinitialisation handler
     *
     * This is where we handle basic loading and populating any missing data in the Repo
     *
     * @param evt The FMLInitializationEvent from FML
     */
    @EventHandler
    public void Init (FMLPreInitializationEvent evt)
    {
        logger.info("Entering initialization phase.");
    }

    /**
     * FML preinitialisation handler
     *
     * Final chance for cleanup before main game launch
     *
     * @param evt The FMLPostInitializationEvent from FML
     */
    @EventHandler
    public void postInit (FMLPreInitializationEvent evt)
    {
        logger.info("Entering postinitialization phase.");
    }

}
