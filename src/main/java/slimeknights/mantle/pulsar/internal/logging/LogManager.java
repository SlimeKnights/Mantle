package slimeknights.mantle.pulsar.internal.logging;

/**
 * Static class for getting generic loggers.
 *
 * @author Arkan <arkan@drakon.io>
 */
public class LogManager
{

    private LogManager()
    {
    } // Singleton.

    private static final boolean useLog4j2;

    /*
     * Detect the presence of log4j2.
     *
     * If it's present, we can assume we should be using the damn thing.
     */
    static
    {
        boolean l4j = false;
        try
        {
            Class cl = Class.forName("org.apache.logging.log4j.LogManager");
            if (cl == null)
            {
                throw new ClassNotFoundException("This should never happen...");
            }
            l4j = true;
        }
        catch (ClassNotFoundException cnfe)
        {
            // Ignore
        }
        useLog4j2 = l4j;
    }

    public static ILogger getLogger(String name)
    {
        if (useLog4j2)
        {
            return new Log4jLogger(name);
        }
        else
        {
            return new JULLogger(name);
        }
    }

}
