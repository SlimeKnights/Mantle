package slimeknights.mantle.pulsar.config;

import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.Loader;

import java.io.File;
import java.util.Locale;

import javax.annotation.Nonnull;

import slimeknights.mantle.pulsar.pulse.PulseMeta;

/**
 * Mantle specific pulsar addon class to support using the forge CFG format for configurations
 * @author progwml6
 */
public class ForgeCFG implements IConfiguration
{

    private Configuration config;

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
        this.description = description.toLowerCase(Locale.US);
    }

    @Override
    public void load()
    {
        config = new Configuration(new File(this.confPath), "1");
        config.load();
    }

    @Override
    public boolean isModuleEnabled(@Nonnull PulseMeta meta)
    {
        Property prop = config.get(this.description, meta.getId(), meta.isEnabled(), meta.getDescription());
        prop.setRequiresMcRestart(true);
        return prop.getBoolean(meta.isEnabled());
    }

    @Override
    public void flush()
    {
        if (config.hasChanged())
        {
            config.save();
        }
    }

    public Configuration getConfig() {
        return config;
    }

    public ConfigCategory getCategory() {
        return config.getCategory(this.description);
    }
}
