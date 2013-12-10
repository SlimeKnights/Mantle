package mantle.world;

import net.minecraftforge.common.ForgeDirection;

/**
 * Helper functions for dealing with ForgeDirection
 *
 * @author Sunstrike <sun@sunstrike.io>
 */
public class DirectionUtils {

    private DirectionUtils() {} // All static

    public static boolean isRightAngles(ForgeDirection a, ForgeDirection b)
    {
        return a != b && a != b.getOpposite() && a != ForgeDirection.UNKNOWN && b != ForgeDirection.UNKNOWN;
    }

}
