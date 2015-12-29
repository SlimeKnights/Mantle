package slimeknights.mantle.pulsar.internal.logging;

import java.util.logging.Logger;

/**
 * Java logger wrapper.
 *
 * @author Arkan <arkan@drakon.io>
 */
public class JULLogger implements ILogger {

    private final Logger log;

    public JULLogger(String name) {
        log = Logger.getLogger(name);
    }

    @Override
    public void fatal(String msg) {
        log.severe(msg); // JUL doesn't -have- fatal...
    }

    @Override
    public void severe(String msg) {
        log.severe(msg);
    }

    @Override
    public void warn(String msg) {
        log.warning(msg);
    }

    @Override
    public void info(String msg) {
        log.info(msg);
    }

    @Override
    public void debug(String msg) {
        log.fine(msg); // Good enough.
    }

    @Override
    public void trace(String msg) {
        log.finest(msg); // Closest we'll get.
    }

}
