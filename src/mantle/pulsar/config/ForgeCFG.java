package mantle.pulsar.config;

import java.io.File;

import mantle.pulsar.pulse.PulseMeta;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Loader;

/**
 * Mantle specific pulsar addon class to support using the forge CFG format for configurations
 * @author progwml6
 */
public class ForgeCFG implements IConfiguration
{

    private static Configuration config;

    private final String confPath;

    private final String description;

    /**
     * Creates a new Configuration object.
     *
     * Do NOT make this the same as the overall mod configuration; it will clobber it!
     *
     * @param confName The config file name (without path or .cfg suffix)
     * @param description The description for the group that the config entries will be placed in.
     */
    public ForgeCFG(String confName, String description)
    {
        this.confPath = Loader.instance().getConfigDir().toString() + File.separator + confName + ".cfg";
        this.description = description;
    }

    @Override
    public void load()
    {
        config = new Configuration(new File(this.confPath));
        config.load();
    }

    @Override
    public boolean isModuleEnabled(PulseMeta meta)
    {
        return config.get(this.description, meta.getId(), meta.isEnabled(), meta.getDescription()).getBoolean(meta.isEnabled());
    }

    @Override
    public void flush()
    {
        if (config.hasChanged())
        {
            config.save();
        }
    }

}
