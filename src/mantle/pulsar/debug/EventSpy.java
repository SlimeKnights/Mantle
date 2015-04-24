package mantle.pulsar.debug;

import mantle.pulsar.internal.logging.ILogger;
import mantle.pulsar.internal.logging.LogManager;
import mantle.pulsar.pulse.Pulse;
import net.minecraftforge.fml.common.Loader;

import com.google.common.eventbus.Subscribe;

/**
 * Debug Pulse to 'eavesdrop' on the PulseManager event bus traffic.
 *
 * @author Arkan <arkan@drakon.io>
 */
@SuppressWarnings("unused")
@Pulse(id = "EventSpy", description = "we iz in ur buses, monitorin ur eventz", forced = true)
public class EventSpy
{

    private final ILogger log = LogManager.getLogger("EventSpy/" + Loader.instance().activeModContainer().getModId());

    @Subscribe
    public void receive(Object evt)
    {
        this.log.info("Received event: " + evt);
    }

}
