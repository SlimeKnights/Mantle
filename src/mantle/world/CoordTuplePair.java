package mantle.world;

import net.minecraft.util.BlockPos;

/**
 * CoordTuplePair
 *
 * @author mDiyo
 */
public class CoordTuplePair
{
    public BlockPos a;
    public BlockPos b;

    public CoordTuplePair(BlockPos a, BlockPos b)
    {
        this.a = a;
        this.b = b;
    }

    public CoordTuplePair(int aX, int aY, int aZ, int bX, int bY, int bZ)
    {
        this.a = new BlockPos(aX, aY, aZ);
        this.b = new BlockPos(bX, bY, bZ);
    }
}