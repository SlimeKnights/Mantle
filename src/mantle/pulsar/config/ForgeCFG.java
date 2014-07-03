package mantle.pulsar.config;

import cpw.mods.fml.common.Loader;
import mantle.pulsar.internal.logging.ILogger;
import mantle.pulsar.pulse.PulseMeta;
import net.minecraftforge.common.config.Configuration;

import java.io.*;
import java.util.Map;

public class ForgeCFG implements IConfiguration{

    private static Configuration config;
    private final String confPath;
    private final String description;

    private Map<String, Boolean> modules;

    /**
     * Creates a new Configuration object.
     *
     * Do NOT make this the same as the overall mod configuration; it will clobber it!
     *
     * @param confName The config file name (without path or .cfg suffix)
     * @param description The description for the group that the config entries will be placed in.
     */
    public ForgeCFG(String confName, String description) {
        this.confPath = Loader.instance().getConfigDir().toString() + File.separator + confName + ".cfg";
        this.description = description;
    }

    @Override
    public void load() {
        config = new Configuration(new File(confPath));
    }

    @Override
    public boolean isModuleEnabled(PulseMeta meta) {
        Boolean enabled = modules.get(meta.getId());
            if (enabled == null) {
                modules.put(meta.getId(), meta.isEnabled());
                enabled = meta.isEnabled();
                config.get(description, meta.getId(), meta.isEnabled(), meta.getDescription()).set(meta.isEnabled());
            }
        return enabled;

    }

    @Override
    public void flush() {
        if(config.hasChanged())
            config.save();
    }

}
