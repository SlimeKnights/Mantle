package mantle.pulsar.internal.logging;

/**
 * Interface for log wrappers.
 *
 * @author Arkan <arkan@drakon.io>
 */
@SuppressWarnings("unused")
public interface ILogger {

    public void fatal(String msg);

    public void severe(String msg);

    public void warn(String msg);

    public void info(String msg);

    public void debug(String msg);

    public void trace(String msg);

}
