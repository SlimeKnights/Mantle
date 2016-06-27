package slimeknights.mantle.block;

import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;

// Mojang made the constructor for stairs protected, so this class is basically a wrapper to change that and fix a lighting error
public class BlockStairsBase extends BlockStairs {

  public BlockStairsBase(IBlockState modelState) {
    super(modelState);
    this.useNeighborBrightness = true;
    this.setCreativeTab(modelState.getBlock().displayOnCreativeTab);
  }
}
