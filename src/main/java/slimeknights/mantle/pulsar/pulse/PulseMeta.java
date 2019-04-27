package slimeknights.mantle.pulsar.pulse;

import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Metadata wrapper for parsed @Pulse metadata.
 *
 * @author Arkan <arkan@drakon.io>
 */
@ParametersAreNonnullByDefault
public class PulseMeta {

    private String id, description;
    private boolean forced, enabled, defaultEnabled;
    private boolean missingDeps = false;
    private BooleanValue configEntry;

    public PulseMeta(String id, @Nullable String description, boolean forced, boolean enabled, boolean defaultEnabled) {
        this.id = id;
        this.description = description;
        this.forced = forced;
        this.enabled = enabled;
        this.defaultEnabled = defaultEnabled;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public boolean isForced() {
        return !missingDeps && forced;
    }

    public boolean isEnabled() {
        return !missingDeps && enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setMissingDeps(boolean missing) {
        missingDeps = missing;
    }

    public boolean isDefaultEnabled() {
        return defaultEnabled;
    }

    public void setConfigEntry(BooleanValue entry) {
        this.configEntry = entry;
    }

    public BooleanValue getConfigEntry() {
        return this.configEntry;
    }
}
