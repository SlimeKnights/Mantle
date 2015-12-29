package slimeknights.mantle.pulsar.internal.logging;

import org.apache.logging.log4j.Logger;

/**
 * Log4j log wrapper.
 *
 * @author Arkan <arkan@drakon.io>
 */
public class Log4jLogger implements ILogger {

    private final Logger log;

    public Log4jLogger(String name) {
        log = org.apache.logging.log4j.LogManager.getLogger(name);
    }

    @Override
    public void fatal(String msg) {
        log.fatal(msg);
    }

    @Override
    public void severe(String msg) {
        log.error(msg);
    }

    @Override
    public void warn(String msg) {
        log.warn(msg);
    }

    @Override
    public void info(String msg) {
        log.info(msg);
    }

    @Override
    public void debug(String msg) {
        log.debug(msg);
    }

    @Override
    public void trace(String msg) {
        log.trace(msg);
    }

}
