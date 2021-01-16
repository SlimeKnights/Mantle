package slimeknights.mantle.multiblock;

import net.minecraft.util.math.BlockPos;

/**
 * @deprecated  Slated for removal in 1.17. If you used this, talk to one of the devs and we can pull the updated verson from Tinkers Construct back
 */
@Deprecated
public interface IMasterLogic {

  /**
   * Called when servants change their state
   *
   * @param pos servant position
   */
  void notifyChange(IServantLogic servant, BlockPos pos);
}
