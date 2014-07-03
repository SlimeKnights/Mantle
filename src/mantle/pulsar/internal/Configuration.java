package mantle.pulsar.internal;

import java.io.*;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import cpw.mods.fml.common.Loader;
import mantle.pulsar.config.IConfiguration;
import mantle.pulsar.internal.logging.ILogger;
import mantle.pulsar.pulse.PulseMeta;

/**
 * Default Gson Configuration helper.
 *
 * @author Arkan <arkan@drakon.io>
 */
public class Configuration implements IConfiguration {

    private static final int CONFIG_LEVEL = 1;

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final String confPath;
    private final ILogger logger;

    private Map<String, ConfigEntry> modules;

    /**
     * Creates a new Configuration object.
     *
     * Do NOT make this the same as the overall mod configuration; it will clobber it!
     *
     * @param confName The config file name (without path or .json suffix)
     * @param logger The logger to send debug info to.
     */
    public Configuration(String confName, ILogger logger) {
        this.confPath = Loader.instance().getConfigDir().toString() + File.separator + confName + ".json";
        this.logger = logger;
    }

    @Override
    public void load() {
        getModulesFromJson();
    }

    @Override
    public boolean isModuleEnabled(PulseMeta meta) {
        ConfigEntry entry = modules.get(meta.getId());
        if (entry == null) {
            modules.put(meta.getId(), new ConfigEntry(meta.isEnabled(), meta.getDescription()));
            return meta.isEnabled();
        } else {
            return entry.getEnabled();
        }
    }

    @Override
    public void flush() {
        writeModulesToJson();
    }

    private void getModulesFromJson() {
        // Step 1: Does the file exist?
        File f = new File(confPath);
        if (!f.exists()) {
            logger.info("Couldn't find config file; will generate a new one later.");
            modules = new HashMap<String, ConfigEntry>();
            return;
        }

        // Step 2: File exists. Let's make sure it's usable.
        if (!(f.canRead() && f.canWrite())) {
            throw new FileNotReadWritableException("Could not read/write Pulsar config: " + confPath);
        }

        // Step 3: Good enough. Read it.
        try {
            try {
                modules = parseV1Config(f);
            } catch (Exception ex) {
                logger.warn("Failed to parse " + f.getName() + " using the v1 parser; trying the v0 parser.");
                Map<String, ConfigEntry> conf = parseV0Config(f);
                logger.info("Found valid v0 configuration. Upgrading it.");
                modules = conf;
                writeModulesToJson();
                logger.info("Upgrade complete! Config is now in v1 format.");
            }
        } catch (Exception ex) {
            logger.warn("Invalid config file. Discarding.");
            ex.printStackTrace();
            modules = new HashMap<String, ConfigEntry>();
        }
    }

    private Map<String, ConfigEntry> parseV0Config(File f) throws Exception {
        try {
            JsonReader reader = new JsonReader(new InputStreamReader(new FileInputStream(f)));
            Map<String, Boolean> m = gson.fromJson(reader, new TypeToken<HashMap<String, Boolean>>(){}.getType()); // NASTY!
            if (m == null) {
                throw new NullPointerException("Gson returned null.");
            }
            Map<String, ConfigEntry> out = new HashMap<String, ConfigEntry>();
            for (Map.Entry<String, Boolean> e : m.entrySet()) {
                out.put(e.getKey(), new ConfigEntry(e.getValue()));
            }
            return out;
        } catch (FileNotFoundException fnfe) {
            throw new RuntimeException("This shouldn't be possible... " + fnfe);
        }
    }

    private Map<String, ConfigEntry> parseV1Config(File f) throws Exception {
        try {
            JsonReader reader = new JsonReader(new InputStreamReader(new FileInputStream(f)));
            GsonConfig c = gson.fromJson(reader, GsonConfig.class);
            if (c.getConfigVersion() > 1) throw new RuntimeException("Pulsar config is from a newer version! Remove it! " + f.getAbsolutePath());
            if (c.getModules() == null) throw new IllegalArgumentException("Not a valid GsonConfig. Try v0 parsing.");
            return c.getModules();
        } catch (FileNotFoundException fnfe) {
            throw new RuntimeException("This shouldn't be possible... " + fnfe);
        }
    }

    private void writeModulesToJson() {
        try {
            JsonWriter writer = new JsonWriter(new OutputStreamWriter(new FileOutputStream(new File(confPath))));
            writer.setIndent("  ");
            GsonConfig out = new GsonConfig(CONFIG_LEVEL, modules);
            gson.toJson(out, GsonConfig.class, writer);
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

    /**
     * Internal data holder, for use in the map and JSON.
     */
    private static class ConfigEntry {

        private Boolean enabled;
        private String description = null;

        public ConfigEntry(Boolean enabled) {
            this.enabled = enabled;
        }

        public ConfigEntry(Boolean enabled, String description) {
            this(enabled);
            this.description = description;
        }

        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Internal representation of a version 1+ config file.
     */
    private static class GsonConfig {

        private int CONFIG_VERSION = 0;

        private Map<String, ConfigEntry> modules;

        public GsonConfig(int version, Map<String, ConfigEntry> modules) {
            this.CONFIG_VERSION = version;
            this.modules = modules;
        }

        public int getConfigVersion() {
            return CONFIG_VERSION;
        }

        public Map<String, ConfigEntry> getModules() {
            return modules;
        }

    }

}
