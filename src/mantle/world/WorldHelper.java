package mantle.world;

import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class WorldHelper
{
    public static boolean setBlockToAir (World w, BlockPos pos)
    {
        return w.setBlockToAir(pos);
    }

    public static boolean isAirBlock (IBlockAccess access, BlockPos pos)
    {
        return access.getBlockState(pos).getBlock().isAir(access, pos);
    }

    public static boolean isAirBlock (World access, BlockPos pos)
    {
        return access.isAirBlock(pos);
    }

}
