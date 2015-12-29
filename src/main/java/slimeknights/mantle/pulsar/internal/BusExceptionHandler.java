package slimeknights.mantle.pulsar.internal;

import slimeknights.mantle.pulsar.flightpath.IExceptionHandler;
import slimeknights.mantle.pulsar.internal.logging.ILogger;
import slimeknights.mantle.pulsar.internal.logging.LogManager;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Custom exception catcher that logs events.
 */
@ParametersAreNonnullByDefault
public final class BusExceptionHandler implements IExceptionHandler {

    private final String id;
    private final ILogger logger;

    /**
     * @param id Mod ID to include in exception raises.
     */
    public BusExceptionHandler(String id) {
        this.id = id;
        this.logger = LogManager.getLogger(id + "-Pulsar-Flightpath");
    }

    @Override
    public void handle(Exception ex) {
        this.logger.severe("Exception caught from a pulse on flightpath for mod ID " + id + ": " + ex);
    }

    @Override
    public void flush() {
        // NO-OP
    }

}
