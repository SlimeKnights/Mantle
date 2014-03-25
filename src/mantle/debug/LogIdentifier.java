package mantle.debug;

import java.io.PrintStream;
import static mantle.lib.CoreRepo.logger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LogIdentifier
{
    private Logger outLogger = LogManager.getLogger("Mantle-STDOUT");
    private Logger errLogger = LogManager.getLogger("Mantle-STDERR");

    public void preinit ()
    {
        logger.info("Inserting TracingPrintStream...");
        System.setOut(new TracingPrintStream(outLogger, "STDOUT", System.out));
        System.setErr(new TracingPrintStream(errLogger, "STDERR", System.err));
        logger.info("TracingPrintStream inserted on STDOUT/STDERR. These will now be redirected to the Mantle-STDOUT/Mantle-STDERR loggers.");
        //logger.info("Forcing Java.util.Logger logging into log4j2");
        //JavaLoggingRedirector.activate();
        //  logger.info("Successfully redirected java.util.logging");
    }

}

class TracingPrintStream extends PrintStream
{
    static Logger l;

    public TracingPrintStream(Logger l, String Stream, PrintStream original)
    {
        super(original);
    }

    @Override
    public void println (String x)
    {
        x = x != null ? x : "";
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        // stack(2) is calling object.
        String name = stack[2].getClassName();
        if (name != null)
            l.info("[" + name + "]: " + x);
        else
            l.info("[" + Thread.currentThread().getName() + "]: " + x);
    }

}
