package mantle.world;

import java.lang.String;

/**
 * CoordTuple
 *
 * @author mDiyo
 */
public class CoordTuple implements Comparable
{
    public final int x;
    public final int y;
    public final int z;

    public CoordTuple(double posX, double posY, double posZ)
    {
        x = (int) Math.floor(posX);
        y = (int) Math.floor(posY);
        z = (int) Math.floor(posZ);
    }

    public CoordTuple(CoordTuple tuple)
    {
        x = tuple.x;
        y = tuple.y;
        z = tuple.z;
    }

    public boolean equalCoords (int posX, int posY, int posZ)
    {
        if (this.x == posX && this.y == posY && this.z == posZ)
            return true;
        else
            return false;
    }

    @Override
    public int hashCode ()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + x;
        result = prime * result + y;
        result = prime * result + z;
        return result;
    }

    public String toString ()
    {
        return "X: " + x + ", Y: " + y + ", Z: " + z;
    }

    @Override
    public int compareTo (Object o)
    {
        if (o == null)
            throw new NullPointerException("Object cannot be null");

        CoordTuple coord = (CoordTuple) o;

        if (x < coord.x)
        {
            return -1;
        }
        if (x > coord.x)
        {
            return 1;
        }
        if (y < coord.y)
        {
            return -1;
        }
        if (y > coord.y)
        {
            return 1;
        }
        if (z < coord.z)
        {
            return -1;
        }
        if (z > coord.z)
        {
            return 1;
        }

        return 0;
    }

}
