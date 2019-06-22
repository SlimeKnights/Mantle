package slimeknights.mantle.pulsar.debug;

import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;

import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import slimeknights.mantle.pulsar.pulse.Pulse;

/**
 * Debug Pulse to 'eavesdrop' on the PulseManager event bus traffic.
 *
 * @author Arkan <arkan@drakon.io>
 */
@SuppressWarnings("unused")
@Pulse(id="EventSpy", description="we iz in ur buses, monitorin ur eventz", forced=true)
public class EventSpy {

    private final Logger log = LogManager.getLogger("EventSpy/" + ModLoadingContext.get().getActiveContainer().getNamespace());

    @SubscribeEvent
    public void receive(Event evt) {
        this.log.info("Received event: " + evt);
    }

    @SubscribeEvent
    public void preInit(final FMLCommonSetupEvent evt) {
        this.log.info("Example preInit hit");
    }

}
