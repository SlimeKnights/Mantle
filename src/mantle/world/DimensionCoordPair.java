package mantle.world;

import java.lang.String;

/**
 * DimensionCoordPair
 *
 * @author mDiyo
 */
public class DimensionCoordPair
{
    public final int dim;
    public final int x;
    public final int z;

    public DimensionCoordPair(int worldID, int posX, int posZ)
    {
        dim = worldID;
        x = posX;
        z = posZ;
    }

    public boolean equalCoords (int worldID, int posX, int posZ)
    {
        if (this.dim == posX && this.x == posX && this.z == posZ)
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
            DimensionCoordPair coord = (DimensionCoordPair) obj;
            if (this.dim == coord.dim && this.x == coord.x && this.z == coord.z)
                return true;
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
        result = prime * result + z;
        return result;
    }

    public String toString ()
    {
        return "Dim: " + dim + ", X: " + x + ", Z: " + z;
    }
}
