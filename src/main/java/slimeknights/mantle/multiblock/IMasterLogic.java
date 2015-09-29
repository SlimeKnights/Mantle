package slimeknights.mantle.multiblock;

import net.minecraft.util.BlockPos;

public interface IMasterLogic
{
    /** Called when servants change their state
     *
     * @param pos servant position
     */
    public void notifyChange(IServantLogic servant, BlockPos pos);
}
