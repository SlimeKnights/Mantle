package mantle.pulsar.internal;

/**
 * Metadata wrapper for parsed @Pulse metadata.
 *
 * @author Arkan <arkan@drakon.io>
 */
public class PulseMeta {

    private String id;
    private boolean forced, enabled;

    public PulseMeta(String id, boolean forced, boolean enabled) {
        this.id = id;
        this.forced = forced;
        this.enabled = enabled;
    }

    public String getId() {
        return id;
    }

    public boolean isForced() {
        return forced;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
