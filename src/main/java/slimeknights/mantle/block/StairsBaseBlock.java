package slimeknights.mantle.block;

import net.minecraft.block.Block;
import net.minecraft.block.StairsBlock;

// Mojang made the constructor for stairs protected, so this class is basically a wrapper to change that
public class StairsBaseBlock extends StairsBlock {

  public StairsBaseBlock(Block model) {
    super(model.getDefaultState(), Block.Properties.from(model));
  }
}
