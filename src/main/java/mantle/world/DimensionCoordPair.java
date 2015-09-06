package mantle.world;

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
        this.dim = worldID;
        this.x = posX;
        this.z = posZ;
    }

    public boolean equalCoords(int worldID, int posX, int posZ)
    {
        return this.dim == posX && this.x == posX && this.z == posZ;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }

        if (this.getClass() == obj.getClass())
        {
            DimensionCoordPair coord = (DimensionCoordPair) obj;
            if (this.dim == coord.dim && this.x == coord.x && this.z == coord.z)
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        final int prime = 37;
        int result = 1;
        result = prime * result + this.dim;
        result = prime * result + this.x;
        result = prime * result + this.z;
        return result;
    }

    @Override
    public String toString()
    {
        return "Dim: " + this.dim + ", X: " + this.x + ", Z: " + this.z;
    }
}
