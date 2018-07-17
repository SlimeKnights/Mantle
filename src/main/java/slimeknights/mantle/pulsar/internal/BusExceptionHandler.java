package slimeknights.mantle.pulsar.internal;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;

import slimeknights.mantle.pulsar.flightpath.IExceptionHandler;

/**
 * Custom exception catcher that logs events.
 */
@ParametersAreNonnullByDefault
public final class BusExceptionHandler implements IExceptionHandler {

    private final String id;
    private final Logger logger;

    /**
     * @param id Mod ID to include in exception raises.
     */
    public BusExceptionHandler(String id) {
        this.id = id;
        this.logger = LogManager.getLogger(id + "-Pulsar-Flightpath");
    }

    @Override
    public void handle(Exception ex) {
        this.logger.error("Exception caught from a pulse on flightpath for mod ID " + id + ": ", ex);
        // if its a runtime exception, we can just throw it as is
        if(ex instanceof RuntimeException) {
          throw (RuntimeException)ex;
        }
        throw new Error(ex);
    }

    @Override
    public void flush() {
        // NO-OP
    }

}
