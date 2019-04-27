package slimeknights.mantle.pulsar.config;

import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import slimeknights.mantle.pulsar.pulse.PulseMeta;

/**
 * Interface for config handlers.
 *
 * @author Arkan <arkan@drakon.io>
 */
@ParametersAreNonnullByDefault
public interface IConfiguration {

    /**
     * Used via @PulseManager to determine the type of config file used.
     *
     * @return Whether the config file is a TOML file or not.
     */
    boolean isUsingTomlConfig();

    /**
     * Perform any configuration loading required.
     */
    void load();

    /**
     * Gets whether the given module is enabled in the config.
     *
     * @param meta The pulse metadata.
     * @return Whether the module is enabled.
     */
    default boolean isModuleEnabled(PulseMeta meta) {
        return false;
    }

    /**
     * Gets whether the given module is enabled in the config.
     * Use this if you are using TOML files (PulsarConfig).
     *
     * @param booleanValue The boolean value returned by getConfigEntry
     * @return Whether the module is enabled.
     */
    default boolean isModuleEnabled(BooleanValue booleanValue) {
        return false;
    }

    /**
     * Gets the specific boolean value for the pulsar module.
     * Use this if you are using TOML files (PulsarConfig).
     *
     * @param meta The pulse metadata.
     * @return The BooleanValue issued by the TOML config
     */
    default BooleanValue getConfigEntry(PulseMeta meta) {
        return null;
    }

    /**
     * Flush configuration to disk/database/whatever.
     */
    void flush();

    /**
     * Used only by TOML configs to set the description to use for the configs.
     */
    default void popBuilder() {}

    /**
     * Used only by TOML configs to clear the description used by the config settings.
     */
    default void pushBuilder() {}
}
