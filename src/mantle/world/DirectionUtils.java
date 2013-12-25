package mantle.world;

import net.minecraftforge.common.util.ForgeDirection;
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

    public static boolean isHorizontal(ForgeDirection a)
    {
        return a == ForgeDirection.EAST || a == ForgeDirection.NORTH || a == ForgeDirection.SOUTH || a == ForgeDirection.WEST;
    }

}
