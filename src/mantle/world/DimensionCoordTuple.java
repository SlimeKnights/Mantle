package mantle.world;

import java.lang.String;

/**
 * DimensionCoordTuple
 *
 * @author mDiyo
 * @author Sunstrike <sun@sunstrike.io>
 */
public class DimensionCoordTuple
{
    public final int dim;
    public final int x;
    public final int y;
    public final int z;

    public DimensionCoordTuple(int worldID, int posX, int posY, int posZ)
    {
        dim = worldID;
        y = posY;
        x = posX;
        z = posZ;
    }

    public boolean equalCoords (int worldID, int posX, int posY, int posZ)
    {
        if (this.dim == worldID && this.x == posX && this.y == posY && this.z == posZ)
            return true;
        else
            return false;
    }

    @Override
    public boolean equals (Object obj)
    {
        if (obj == null)
            return false;

        if (getClass() == obj.getClass())
        {
            DimensionCoordTuple coord = (DimensionCoordTuple) obj;
            return equalCoords(coord.dim, coord.x, coord.y, coord.z);
        }
        return false;
    }

    @Override
    public int hashCode ()
    {
        final int prime = 37;
        int result = 1;
        result = prime * result + dim;
        result = prime * result + x;
        result = prime * result + y;
        result = prime * result + z;
        return result;
    }

    public String toString ()
    {
        return "Dim: " + dim + ", X: " + x + ", Y: " + y + ", Z: " + z;
    }
}
