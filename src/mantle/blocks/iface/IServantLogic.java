package mantle.blocks.iface;

import mantle.world.CoordTuple;
import net.minecraft.world.World;

public interface IServantLogic
{
    public CoordTuple getMasterPosition ();

    /** The block should already have a valid master */
    public void notifyMasterOfChange ();

    /** Checks if this block can be tied to this master
     * 
     * @param master
     * @param world the world of master
     * @param xMaster xCoord of master
     * @param yMaster yCoord of master
     * @param zMaster zCoord of master
     * @return whether the servant can be tied to this master
     */

    public boolean setPotentialMaster (IMasterLogic master, World world, int xMaster, int yMaster, int zMaster);

    /** Used to set and verify that this is the block's master
     * 
     * @param master
     * @param world 
     * @param xMaster xCoord of master
     * @param yMaster yCoord of master
     * @param zMaster zCoord of master
     * @return Is this block tied to this master?
     */

    public boolean verifyMaster (IMasterLogic master, World world, int xMaster, int yMaster, int zMaster);

    /** Exactly what it says on the tin
     * 
     * @param master
     * @param world 
     * @param xMaster xCoord of master
     * @param yMaster yCoord of master
     * @param zMaster zCoord of master
     */

    public void invalidateMaster (IMasterLogic master, World world, int xMaster, int yMaster, int zMaster);
}
