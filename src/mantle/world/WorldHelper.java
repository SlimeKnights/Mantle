package mantle.world;

import mantle.common.ComparisonHelper;
import net.minecraft.init.Blocks;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class WorldHelper
{
    public static void setBlockToAir (World w, int x, int y, int z)
    {
        w.func_147465_d(x, y, z, Blocks.air, 0, 0);
    }

    public static boolean setBlockToAirBool (World w, int x, int y, int z)
    {
        return w.func_147465_d(x, y, z, Blocks.air, 0, 0);
    }

    public static boolean isAirBlock (IBlockAccess access, int x, int y, int z)
    {
        return ComparisonHelper.areEquivalent(access.func_147439_a(x, y, z), Blocks.air);
    }

}
