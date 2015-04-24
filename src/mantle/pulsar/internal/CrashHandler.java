package mantle.pulsar.internal;

import mantle.pulsar.control.PulseManager;
import mantle.pulsar.pulse.PulseMeta;
import net.minecraftforge.fml.common.ICrashCallable;

/**
 * FML crash callable for Pulse Managers; dumps a list of loaded pulses to the error log.
 *
 * @author Arkan <arkan@drakon.io>
 */
public class CrashHandler implements ICrashCallable
{

    private String id;

    private PulseManager manager;

    public CrashHandler(String modId, PulseManager manager)
    {
        this.id = "Pulsar/" + modId + " loaded Pulses";
        this.manager = manager;
    }

    @Override
    public String getLabel()
    {
        return this.id;
    }

    @Override
    public String call() throws Exception
    {
        String out = "\n";
        for (PulseMeta meta : this.manager.getAllPulseMetadata())
        {
            String state = getStateFromMeta(meta);
            out += "\t\t- " + meta.getId() + " (" + state + ")\n"; // Yes, yes, manual indenting, I know...
        }
        return out;
    }

    private static String getStateFromMeta(PulseMeta meta)
    {
        if (meta.isForced())
        {
            return "Enabled/Forced";
        }
        else
        {
            if (meta.isEnabled())
            {
                return "Enabled/Not Forced";
            }
            else
            {
                return "Disabled/Not Forced";
            }
        }
    }

}
