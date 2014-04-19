package mantle.module;

import cpw.mods.fml.common.Loader;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.ArrayList;

/**
 * Controller for mod compat modules.
 *
 * @author Sunstrike <sun@sunstrike.io>
 * @author Arkan Emberwalker <arkan@emberwalker.cc>
 */
public class ModuleController {

    private enum State {
        WAITING, PREINIT, INIT, POSTINIT, DONE
    }

    private ArrayList<IModule> modules = new ArrayList<IModule>();
    private State currentState = State.WAITING;
    private Logger logger;
    private Configuration config;

    /**
     * Constructor for a config-less plugin loader
     *
     * @param modName The human-readable name of the invoking mod. Used for logger construction.
     */
    public ModuleController(String modName) {
        this.logger = LogManager.getLogger(modName + "-ModuleController");
        this.config = null;
    }

    /**
     * Constructor for a config-using plugin loader
     *
     * @param confName The name for the configuration file (should end in .cfg)
     * @param modName The human-readable name of the invoking mod. Used for logger construction.
     */
    public ModuleController(String confName, String modName) {
        this.logger = LogManager.getLogger(modName + "-ModuleController");
        this.config = new Configuration(new File(Loader.instance().getConfigDir().toString() + File.separator + confName));
    }

    /**
     * The host mod should invoke this when it receives the FML PreInitialization event.
     */
    public void preInit() {
        currentState = State.PREINIT;
        for (IModule m : modules) m.preInit();
    }

    /**
     * The host mod should invoke this when it receives the FML Initialization event.
     */
    public void init() {
        currentState = State.INIT;
        for (IModule m : modules) m.init();
    }

    /**
     * The host mod should invoke this when it receives the FML PostInitialization event.
     */
    public void postInit() {
        currentState = State.POSTINIT;
        for (IModule m : modules) m.postInit();
        currentState = State.DONE;
    }

    /**
     * Main registration point for modules.
     *
     * This version will always check the configuration, if one is defined, before attempting to load a plugin. This is
     * the preferred entry point. To bypass the config test, use registerUncheckedModule().
     *
     * @param mod The module to attempt to register.
     * @return True on success, false on error or rejection.
     */
    public boolean registerModule(Class<? extends IModule> mod) {
        String mID = getModId(mod);
        if (mID == null || !Loader.isModLoaded(mID)) return false;

        boolean allowedByConf = true;
        if (config != null) {
            config.load();
            allowedByConf = config.get("Modules", mID, true).getBoolean(true);
            config.save();
        }

        return allowedByConf && doSetup(mod, mID);
    }

    /**
     * Unchecked handler for registering a module forcefully.
     *
     * This version bypasses any config test; use registerModule where possible. This is only intended for modules
     * the user is not allowed to configure (e.g. Mystcraft blacklist plugins).
     *
     * @param mod The module to attempt to register.
     * @return True on success, false on error.
     */
    public boolean registerUncheckedModule(Class<? extends IModule> mod) {
        String mID = getModId(mod);
        if (mID == null || !Loader.isModLoaded(mID)) return false;

        return doSetup(mod, mID);
    }

    private String getModId(Class<? extends IModule> mod) {
        String mID;
        try {
            mID = (String)mod.getField("modId").get(null);
        } catch (Exception e) {
            logger.error("Module loading failed for class " + mod + "; modId field may be missing.");
            return null;
        }
        return mID;
    }

    /**
     * Internal handler which actually does the heavy lifting with loading.
     *
     * @param mod Module class to setup
     * @return True on success, else false.
     */
    private boolean doSetup(Class<? extends IModule> mod, String mID) {
        IModule module;
        try {
            module = mod.newInstance();
            modules.add(module);
        } catch (Exception e) {
            logger.error("Could not construct module for " + mID);
            logger.error(e.getMessage());
            logger.error(e.getStackTrace());
            return false;
        }

        logger.info("Module for " + mID + " loaded successfully.");

        boolean caughtUp = false;
        switch (currentState) {
            case DONE:
            case POSTINIT:
                module.preInit();
                module.init();
                module.postInit();
                caughtUp = true;
                break;
            case INIT:
                module.preInit();
                module.init();
                caughtUp = true;
                break;
            case PREINIT:
                module.preInit();
                caughtUp = true;
                break;
            default:
                break;
        }

        if (caughtUp) logger.info("Completed catch-up for " + mID + " module.");

        return true;
    }

}