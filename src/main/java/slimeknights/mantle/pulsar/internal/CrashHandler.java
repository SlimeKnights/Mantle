package slimeknights.mantle.pulsar.internal;

import net.minecraftforge.fml.common.ICrashCallable;

import slimeknights.mantle.pulsar.control.PulseManager;
import slimeknights.mantle.pulsar.pulse.PulseMeta;

/**
 * FML crash callable for Pulse Managers; dumps a list of loaded pulses to the error log.
 *
 * @author Arkan <arkan@drakon.io>
 */
public class CrashHandler implements ICrashCallable {

    private String id;
    private PulseManager manager;

    public CrashHandler(String modId, PulseManager manager) {
        this.id = "Pulsar/" + modId + " loaded Pulses";
        this.manager = manager;
    }

    @Override
    public String getLabel() {
        return id;
    }

    @Override
    public String call() throws Exception {
        String out = "\n";
        for (PulseMeta meta : manager.getAllPulseMetadata()) {
            String state = getStateFromMeta(meta);
            out += "\t\t- " + meta.getId() + " (" + state + ")\n"; // Yes, yes, manual indenting, I know...
        }
        return out;
    }

    private static String getStateFromMeta(PulseMeta meta) {
        if (meta.isForced()) {
            return "Enabled/Forced";
        } else {
            if (meta.isEnabled()) {
                return "Enabled/Not Forced";
            } else {
                return "Disabled/Not Forced";
            }
        }
    }

}
