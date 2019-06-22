package slimeknights.mantle.pulsar.pulse;

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

    public PulseMeta(String id, @Nullable String description, boolean forced, boolean enabled, boolean defaultEnabled) {
        this.id = id;
        this.description = description;
        this.forced = forced;
        this.enabled = enabled;
        this.defaultEnabled = defaultEnabled;
    }

    public String getId() {
        return this.id;
    }

    public String getDescription() {
        return this.description;
    }

    public boolean isForced() {
        return !this.missingDeps && this.forced;
    }

    public boolean isEnabled() {
        return !this.missingDeps && this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setMissingDeps(boolean missing) {
        this.missingDeps = missing;
    }

    public boolean isDefaultEnabled() {
        return this.defaultEnabled;
    }
}
