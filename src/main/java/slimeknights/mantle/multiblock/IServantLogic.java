package slimeknights.mantle.multiblock;

import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public interface IServantLogic
{
    public BlockPos getMasterPosition();

    /** The block should already have a valid master */
    public void notifyMasterOfChange();

    /** Checks if this block can be tied to this master
     *
     * @param master
     * @param world the world of master
     * @param pos position of master
     * @return whether the servant can be tied to this master
     */

    public boolean setPotentialMaster(IMasterLogic master, World world, BlockPos pos);

    /** Used to set and verify that this is the block's master
     *
     * @param master
     * @param world
     * @param pos position of master
     * @return Is this block tied to this master?
     */

    public boolean verifyMaster(IMasterLogic master, World world, BlockPos pos);

    /** Exactly what it says on the tin
     *
     * @param master
     * @param world
     * @param pos position of master
     */

    public void invalidateMaster(IMasterLogic master, World world, BlockPos pos);
}
