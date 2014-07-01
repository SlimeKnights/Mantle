package mantle.pulsar.internal;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import cpw.mods.fml.common.Loader;

/**
 * Gson Configuration helper.
 *
 * @author Arkan <arkan@drakon.io>
 */
public class Configuration {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final String confPath;
    private final Logger logger;

    private Map<String, ModuleConfig> modules;

    /**
     * Creates a new Configuration object.
     *
     * Do NOT make this the same as the overall mod configuration; it will clobber it!
     *
     * @param confName The config file name (without path or .json suffix)
     * @param logger The logger to send debug info to.
     */
    public Configuration(String confName, Logger logger) {
        this.confPath = Loader.instance().getConfigDir().toString() + File.separator + confName + ".json";
        this.logger = logger;
        this.modules = getModulesFromJson();
    }

    /**
     * Gets whether the given module is enabled in the config.
     *
     * @param id Module to lookup.
     * @param defaultState Whether the module should be enabled or disabled by default (used to push new entries)
     * @param defaultDescription The default description if the value does not exist already
     * @return Whether the module is enabled.
     */
    public boolean isModuleEnabled(String id, boolean defaultState, String defaultDescription) {
        ModuleConfig modConfig = modules.get(id);
        if (modConfig == null) {
            modules.put(id, new ModuleConfig(defaultState, defaultDescription));
            writeModulesToJson();
             return defaultState;
        }
        return modConfig.enabled;
    }

   @Deprecated
   public boolean isModuleEnabled(String id, boolean defaultState)
   {
       return isModuleEnabled(id, defaultState, null);
   }

    private Map<String, ModuleConfig> getModulesFromJson() {
        // Step 1: Does the file exist?
        File f = new File(confPath);
        if (!f.exists()) {
            logger.info("Couldn't find config file; will generate a new one later.");
            return new HashMap<String, ModuleConfig>();
        }

        // Step 2: File exists. Let's make sure it's usable.
        if (!(f.canRead() && f.canWrite())) {
            throw new FileNotReadWritableException("Could not read/write Pulsar config: " + confPath);
        }

        // Step 3: Good enough. Read it.
        try {
            JsonReader reader = new JsonReader(new InputStreamReader(new FileInputStream(f)));
            Map<String, ModuleConfig> m = gson.fromJson(reader, new TypeToken<HashMap<String, ModuleConfig>>(){}.getType()); // NASTY!
            if (m == null) {
                throw new NullPointerException("Gson returned null.");
            }
            return m;
        } catch (FileNotFoundException fnfe) {
            throw new RuntimeException("This shouldn't be possible... " + fnfe);
        } catch (Exception ex) {
            logger.warn("Invalid config file. Discarding.");
            return new HashMap<String, ModuleConfig>();
        }
    }

    private void writeModulesToJson() {
        try {
            JsonWriter writer = new JsonWriter(new OutputStreamWriter(new FileOutputStream(new File(confPath))));
            writer.setIndent("  ");
            Type t = new TypeToken<Map<String, ModuleConfig>>(){}.getType();
            gson.toJson(modules, t, writer);
            writer.close();
        } catch (Exception ex) {
            logger.warn("Could not write config? " + confPath);
        }
    }

    /**
     * Internal exception for an unreadable/unwritable config.
     */
    private static class FileNotReadWritableException extends RuntimeException {
        public FileNotReadWritableException(String message) {
            super(message);
        }
    }
    private static class ModuleConfig
    {
        public boolean enabled = true;
        public String description = null;
        public ModuleConfig(boolean enabled, String description)
        {
            this.enabled = enabled;
            if (description == null || description.isEmpty())
                this.description = null;
            else
                this.description = description;
        }
    }
}
