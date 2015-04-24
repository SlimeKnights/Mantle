package mantle.world;

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
        this.dim = worldID;
        this.y = posY;
        this.x = posX;
        this.z = posZ;
    }

    public boolean equalCoords(int worldID, int posX, int posY, int posZ)
    {
        return this.dim == worldID && this.x == posX && this.y == posY && this.z == posZ;
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
            DimensionCoordTuple coord = (DimensionCoordTuple) obj;
            return this.equalCoords(coord.dim, coord.x, coord.y, coord.z);
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
        result = prime * result + this.y;
        result = prime * result + this.z;
        return result;
    }

    @Override
    public String toString()
    {
        return "Dim: " + this.dim + ", X: " + this.x + ", Y: " + this.y + ", Z: " + this.z;
    }
}
