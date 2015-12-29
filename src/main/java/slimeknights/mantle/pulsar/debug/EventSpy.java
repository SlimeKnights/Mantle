package slimeknights.mantle.pulsar.debug;

import com.google.common.eventbus.Subscribe;
import slimeknights.mantle.pulsar.internal.logging.ILogger;
import slimeknights.mantle.pulsar.internal.logging.LogManager;
import slimeknights.mantle.pulsar.pulse.Pulse;
import net.minecraftforge.fml.common.Loader;

/**
 * Debug Pulse to 'eavesdrop' on the PulseManager event bus traffic.
 *
 * @author Arkan <arkan@drakon.io>
 */
@SuppressWarnings("unused")
@Pulse(id="EventSpy", description="we iz in ur buses, monitorin ur eventz", forced=true)
public class EventSpy {

    private final ILogger log = LogManager.getLogger("EventSpy/" + Loader.instance().activeModContainer().getModId());

    @Subscribe
    public void receive(Object evt) {
        log.info("Received event: " + evt);
    }

}
