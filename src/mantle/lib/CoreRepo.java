package mantle.lib;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Mantle object repository
 *
 * Storage area for objects accessible throughout Mantle, such as loggers
 *
 * @author Sunstrike <sun@sunstrike.io>
 */
public final class CoreRepo
{

    public static final String modId = "Mantle";
    public static final String modName = "Mantle";
    public static final String modVersion = "${version}";

    public static final Logger logger = LogManager.getLogger("Mantle");

}
