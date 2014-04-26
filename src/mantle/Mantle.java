package mantle;

import static mantle.lib.CoreRepo.logger;
import static mantle.lib.CoreRepo.modId;
import static mantle.lib.CoreRepo.modName;
import static mantle.lib.CoreRepo.modVersion;
import mantle.books.BookData;
import mantle.books.BookDataStore;
import mantle.common.IDDumps;
import mantle.common.MProxyCommon;
import mantle.debug.LogIdentifier;
import mantle.items.Manual;
import mantle.lib.CoreConfig;
import mantle.lib.CoreRepo;
import mantle.lib.environment.EnvironmentChecks;
import mantle.module.ModuleController;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;

import java.io.File;

/**
 * Mantle
 *
 * Central mod object for Mantle
 *
 * @author Sunstrike <sun@sunstrike.io>
 */
@Mod(modid = modId, name = modName, version = modVersion, dependencies = "required-after:Forge")
public class Mantle
{
    /* Instance of this mod, used for grabbing prototype fields */
    @Instance("Mantle")
    public static Mantle instance;
    /* Proxies for sides, used for graphics processing */
    @SidedProxy(clientSide = "mantle.client.MProxyClient", serverSide = "mantle.common.MProxyCommon")

    public static MProxyCommon proxy;
    public static Manual mantleBook;
    public static LogIdentifier logID;
    public static ModuleController moduleLoader = new ModuleController("SlimeKnights" + File.separator + "Mantle-Modules.cfg", "Mantle");

    /**
     * Constructor
     *
     * EnvChecks invoked here so they run as soon as possible to enhance crash reports that may happen in preinit.
     */
    public Mantle()
    {
        EnvironmentChecks.verifyEnvironmentSanity();
    }

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
        logger.info("Mantle (" + modVersion + ") -- Preparing for launch.");
        logger.info("Entering preinitialization phase.");
        CoreConfig.loadConfiguration(evt.getModConfigurationDirectory());

        if (CoreConfig.identifyLogs) {
            logID = new LogIdentifier();
            logID.preinit();
        }
        NetworkRegistry.INSTANCE.registerGuiHandler(instance, proxy);

        mantleBook = (Manual) new Manual().setUnlocalizedName("mantle.manual");
        GameRegistry.registerItem(mantleBook, "mantleBook");

        moduleLoader.preInit();
    }

    /**
     * FML initialisation handler
     *
     * This is where we handle basic loading and populating any missing data in the Repo
     *
     * @param evt The FMLInitializationEvent from FML
     */
    @EventHandler
    public void Init (FMLInitializationEvent evt)
    {
        logger.info("Entering initialization phase.");
        proxy.registerRenderer();

        moduleLoader.init();
    }

    /**
     * FML postinitialisation handler
     *
     * Final chance for cleanup before main game launch
     *
     * @param evt The FMLPostInitializationEvent from FML
     */
    @EventHandler
    public void postInit (FMLPostInitializationEvent evt)
    {
        logger.info("Entering postinitialization phase.");
        proxy.readManuals();
        BookData data = new BookData();
        data.unlocalizedName = "item.mantle.manual.test";
        data.toolTip = "Test Book";
        data.modID = CoreRepo.modId;
        BookDataStore.addBook(data);
        mantleBook.updateManual();
        CoreConfig.loadBookLocations();
        IDDumps.dump();

        moduleLoader.postInit();
    }

}
