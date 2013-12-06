package mantle.lib;

import java.util.logging.Logger;

/**
 * Mantle object repository
 *
 * Storage area for objects accessible throughout Mantle, such as loggers
 *
 * @author Sunstrike <sunstrike@azurenode.net>
 */
public final class CoreRepo
{

    public static final String modId = "Mantle";
    public static final String modName = "Mantle";
    public static final String modVersion = "${version}";

    public static final Logger logger = Logger.getLogger(modId);

}
