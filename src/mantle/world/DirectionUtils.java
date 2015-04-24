package mantle.world;

import net.minecraft.util.EnumFacing;

/**
 * Helper functions for dealing with ForgeDirection
 *
 * @author Sunstrike <sun@sunstrike.io>
 */
@SuppressWarnings("unused")
public class DirectionUtils
{

    private DirectionUtils()
    {
        // No instantiation
    }

    public static boolean isRightAngles(EnumFacing a, EnumFacing b)
    {
        return a != b && a != b.getOpposite();
    }

    public static boolean isHorizontal(EnumFacing a)
    {
        return a == EnumFacing.EAST || a == EnumFacing.NORTH || a == EnumFacing.SOUTH || a == EnumFacing.WEST;
    }

}
