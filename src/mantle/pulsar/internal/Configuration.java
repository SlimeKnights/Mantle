package mantle.pulsar.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

import mantle.pulsar.config.IConfiguration;
import mantle.pulsar.internal.logging.ILogger;
import mantle.pulsar.pulse.PulseMeta;
import net.minecraftforge.fml.common.Loader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * Default Gson Configuration helper.
 *
 * @author Arkan <arkan@drakon.io>
 */
public class Configuration implements IConfiguration
{

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
    public Configuration(String confName, ILogger logger)
    {
        this.confPath = Loader.instance().getConfigDir().toString() + File.separator + confName + ".json";
        this.logger = logger;
    }

    @Override
    public void load()
    {
        this.getModulesFromJson();
    }

    @Override
    public boolean isModuleEnabled(PulseMeta meta)
    {
        ConfigEntry entry = this.modules.get(meta.getId());
        if (entry == null)
        {
            this.modules.put(meta.getId(), new ConfigEntry(meta.isDefaultEnabled(), meta.getDescription()));
            return meta.isEnabled();
        }
        else
        {
            return entry.getEnabled();
        }
    }

    @Override
    public void flush()
    {
        this.writeModulesToJson();
    }

    private void getModulesFromJson()
    {
        // Step 1: Does the file exist?
        File f = new File(this.confPath);
        if (!f.exists())
        {
            this.logger.info("Couldn't find config file; will generate a new one later.");
            this.modules = new HashMap<String, ConfigEntry>();
            return;
        }

        // Step 2: File exists. Let's make sure it's usable.
        if (!(f.canRead() && f.canWrite()))
        {
            throw new FileNotReadWritableException("Could not read/write Pulsar config: " + this.confPath);
        }

        // Step 3: Good enough. Read it.
        try
        {
            try
            {
                this.modules = this.parseV1Config(f);
            }
            catch (Exception ex)
            {
                this.logger.warn("Failed to parse " + f.getName() + " using the v1 parser; trying the v0 parser.");
                Map<String, ConfigEntry> conf = this.parseV0Config(f);
                this.logger.info("Found valid v0 configuration. Upgrading it.");
                this.modules = conf;
                this.writeModulesToJson();
                this.logger.info("Upgrade complete! Config is now in v1 format.");
            }
        }
        catch (Exception ex)
        {
            this.logger.warn("Invalid config file. Discarding.");
            ex.printStackTrace();
            this.modules = new HashMap<String, ConfigEntry>();
        }
    }

    private Map<String, ConfigEntry> parseV0Config(File f) throws Exception
    {
        try
        {
            JsonReader reader = new JsonReader(new InputStreamReader(new FileInputStream(f)));
            Map<String, Boolean> m = gson.fromJson(reader, new TypeToken<HashMap<String, Boolean>>()
                    {
                    }.getType()); // NASTY!
            if (m == null)
            {
                throw new NullPointerException("Gson returned null.");
            }
            Map<String, ConfigEntry> out = new HashMap<String, ConfigEntry>();
            for (Map.Entry<String, Boolean> e : m.entrySet())
            {
                out.put(e.getKey(), new ConfigEntry(e.getValue()));
            }
            return out;
        }
        catch (FileNotFoundException fnfe)
        {
            throw new RuntimeException("This shouldn't be possible... " + fnfe);
        }
    }

    private Map<String, ConfigEntry> parseV1Config(File f) throws Exception
    {
        try
        {
            JsonReader reader = new JsonReader(new InputStreamReader(new FileInputStream(f)));
            GsonConfig c = gson.fromJson(reader, GsonConfig.class);
            if (c.getConfigVersion() > 1)
            {
                throw new RuntimeException("Pulsar config is from a newer version! Remove it! " + f.getAbsolutePath());
            }
            if (c.getModules() == null)
            {
                throw new IllegalArgumentException("Not a valid GsonConfig. Try v0 parsing.");
            }
            return c.getModules();
        }
        catch (FileNotFoundException fnfe)
        {
            throw new RuntimeException("This shouldn't be possible... " + fnfe);
        }
    }

    private void writeModulesToJson()
    {
        try
        {
            JsonWriter writer = new JsonWriter(new OutputStreamWriter(new FileOutputStream(new File(this.confPath))));
            writer.setIndent("  ");
            GsonConfig out = new GsonConfig(CONFIG_LEVEL, this.modules);
            gson.toJson(out, GsonConfig.class, writer);
            writer.close();
        }
        catch (Exception ex)
        {
            this.logger.warn("Could not write config? " + this.confPath);
        }
    }

    /**
     * Internal exception for an unreadable/unwritable config.
     */
    private static class FileNotReadWritableException extends RuntimeException
    {
        public FileNotReadWritableException(String message)
        {
            super(message);
        }
    }

    /**
     * Internal data holder, for use in the map and JSON.
     */
    private static class ConfigEntry
    {

        private Boolean enabled;

        private String description = null;

        public ConfigEntry(Boolean enabled)
        {
            this.enabled = enabled;
        }

        public ConfigEntry(Boolean enabled, String description)
        {
            this(enabled);
            this.description = description;
        }

        public Boolean getEnabled()
        {
            return this.enabled;
        }
    }

    /**
     * Internal representation of a version 1+ config file.
     */
    private static class GsonConfig
    {

        private int CONFIG_VERSION = 0;

        private Map<String, ConfigEntry> modules;

        public GsonConfig(int version, Map<String, ConfigEntry> modules)
        {
            this.CONFIG_VERSION = version;
            this.modules = modules;
        }

        public int getConfigVersion()
        {
            return this.CONFIG_VERSION;
        }

        public Map<String, ConfigEntry> getModules()
        {
            return this.modules;
        }

    }

}
