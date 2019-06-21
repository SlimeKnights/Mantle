package slimeknights.mantle.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.StairsBlock;

// Mojang made the constructor for stairs protected, so this class is basically a wrapper to change that and fix a lighting error
public class BlockStairsBase extends StairsBlock
{

  public BlockStairsBase(Block model) {
    super(model.getDefaultState(), Block.Properties.from(model));
  }
}
