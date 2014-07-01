package mantle.pulsar.internal;

/**
 * Metadata wrapper for parsed @Pulse metadata.
 *
 * @author Arkan <arkan@drakon.io>
 */
public class PulseMeta {

    private String id, description;
    private boolean forced, enabled;

    public PulseMeta(String id, boolean forced, boolean enabled, String description) {
        this.id = id;
        this.forced = forced;
        this.enabled = enabled;
        setDescription(description);
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

    public void setDescription(String description)
    {
        if(description == null || description.isEmpty())
            this.description = null;
        else
            this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
