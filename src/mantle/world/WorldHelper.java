package mantle.world;

import mantle.common.ComparisonHelper;
import net.minecraft.init.Blocks;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class WorldHelper
{
    public static void setBlockToAir (World w, int x, int y, int z)
    {
        w.setBlock(x, y, z, Blocks.air, 0, 0);
    }

    public static boolean setBlockToAirBool (World w, int x, int y, int z)
    {
        return w.setBlock(x, y, z, Blocks.air, 0, 0);
    }

    public static boolean isAirBlock (IBlockAccess access, int x, int y, int z)
    {
        return ComparisonHelper.areEquivalent(access.getBlock(x, y, z), Blocks.air);
    }

    public static boolean isAirBlock (World access, int x, int y, int z)
    {
        return ComparisonHelper.areEquivalent(access.getBlock(x, y, z), Blocks.air);
    }

}
