package slimeknights.mantle.pulsar.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import slimeknights.mantle.pulsar.control.PulseManager;
import slimeknights.mantle.pulsar.pulse.PulseMeta;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class PulsarConfig implements IConfiguration {

  private static final Logger LOGGER = LogManager.getLogger();

  private final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
  private ForgeConfigSpec SPEC = this.BUILDER.build();

  private final CommentedFileConfig config;

  private final String description;

  private boolean configBuilt;
  private final Map<String, ForgeConfigSpec.BooleanValue> modules = new LinkedHashMap<>();

  /**
   * Creates the config to be used later.
   *
   * Do NOT make this the same as the overall mod configuration; it will clobber it!
   *
   * @param confName The config file name (without path or .toml suffix)
   * @param description The description for the group that the config entries will be placed in.
   */
  public PulsarConfig(String confName, String description) {
    this.config = this.createConfig(FMLPaths.CONFIGDIR.get(), confName);
    this.description = description.toLowerCase(Locale.US);
    this.configBuilt = false;
    this.BUILDER.push(this.description);
  }

  @Override
  public void load() {
  }

  @Override
  public void postLoad() {
    if (!this.configBuilt) {
      this.BUILDER.pop();
      this.SPEC = this.BUILDER.build();
      this.SPEC.setConfig(this.config);
    }

    this.configBuilt = true;
  }

  @Override
  public boolean isModuleEnabled(@Nonnull PulseMeta meta) {
    ForgeConfigSpec.BooleanValue entry = this.modules.get(meta.getId());

    if (entry == null) {
      this.modules.put(meta.getId(), this.BUILDER.comment(meta.getDescription()).worldRestart().define(meta.getId(), meta.isDefaultEnabled()));
      return meta.isEnabled();
    }
    else {
      return entry.get();
    }
  }

  @Override
  public void addPulse(@Nonnull PulseMeta meta) {
    ForgeConfigSpec.BooleanValue entry = this.modules.get(meta.getId());
    if (entry == null) {
      this.modules.put(meta.getId(), this.BUILDER.comment(meta.getDescription()).worldRestart().define(meta.getId(), meta.isDefaultEnabled()));
    }
  }

    /*@Override
    public boolean isModuleEnabled(ForgeConfigSpec.BooleanValue configValue) {
        return configValue.get();
    }

    @Override
    public ForgeConfigSpec.BooleanValue getConfigEntry(PulseMeta meta) {
        ForgeConfigSpec.BooleanValue value = BUILDER.comment(meta.getDescription()).worldRestart().define(meta.getId(), meta.isDefaultEnabled());

        return value;
    }*/

  @Override
  public void flush() {
    this.config.save();
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
