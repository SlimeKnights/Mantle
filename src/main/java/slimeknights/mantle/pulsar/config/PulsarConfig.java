package slimeknights.mantle.pulsar.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import slimeknights.mantle.pulsar.control.PulseManager;
import slimeknights.mantle.pulsar.pulse.PulseMeta;

import java.nio.file.Path;
import java.util.Locale;

public class PulsarConfig implements IConfiguration
{
    private static final Logger LOGGER = LogManager.getLogger();

    private final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    private ForgeConfigSpec SPEC = BUILDER.build();

    private final CommentedFileConfig config;

    private final String description;

    private boolean builderPushed;
    private boolean configBuilt;

    /**
     * Creates the config to be used later.
     *
     * Do NOT make this the same as the overall mod configuration; it will clobber it!
     *
     * @param confName The config file name (without path or .toml suffix)
     * @param description The description for the group that the config entries will be placed in.
     */
    public PulsarConfig(String confName, String description) {
        this.config = createConfig(FMLPaths.CONFIGDIR.get(), confName);
        this.description = description.toLowerCase(Locale.US);
        this.builderPushed = false;
        this.configBuilt = false;
    }

    @Override
    public boolean isUsingTomlConfig() {
        return true;
    }

    @Override
    public void load() {
        if(!this.configBuilt) {
            SPEC = BUILDER.build();
            SPEC.setConfig(config);
        }

        configBuilt = true;
    }

    @Override
    public void pushBuilder() {
        if(!builderPushed) BUILDER.push(this.description);

        builderPushed = true;
    }

    @Override
    public void popBuilder() {
        BUILDER.pop();
    }

    @Override
    public boolean isModuleEnabled(ForgeConfigSpec.BooleanValue configValue) {
        return configValue.get();
    }

    @Override
    public ForgeConfigSpec.BooleanValue getConfigEntry(PulseMeta meta) {
        ForgeConfigSpec.BooleanValue value = BUILDER.comment(meta.getDescription()).worldRestart().define(meta.getId(), meta.isDefaultEnabled());

        return value;
    }

    @Override
    public void flush() {
        ((CommentedFileConfig)this.config).save();
    }

    public CommentedFileConfig createConfig(Path configBasePath, String configName) {
        final Path configPath = configBasePath.resolve(String.format("%s.toml", configName));
        final CommentedFileConfig configData = CommentedFileConfig.builder(configPath).sync().autosave().writingMode(WritingMode.REPLACE).build();
        LOGGER.debug(PulseManager.CONFIG, "Built TOML config for {}", configPath.toString());
        configData.load();
        LOGGER.debug(PulseManager.CONFIG, "Loaded TOML config file {}", configPath.toString());
        return configData;
    }
}
