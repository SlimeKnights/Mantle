package mantle.world;

import mantle.common.ComparisonHelper;
import net.minecraft.init.Blocks;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class WorldHelper
{
    @Deprecated
    public static boolean setBlockToAirBool (World w, int x, int y, int z)
    {
        return setBlockToAir(w, x, y, z);
    }

    public static boolean setBlockToAir (World w, int x, int y, int z)
    {
        return w.setBlockToAir(x, y, z);
    }

    public static boolean isAirBlock (IBlockAccess access, int x, int y, int z)
    {
        return access.getBlock(x,y,z).isAir(access, x,y,z);
    }

    public static boolean isAirBlock (World access, int x, int y, int z)
    {
        return access.isAirBlock(x,y,z);
    }

}
