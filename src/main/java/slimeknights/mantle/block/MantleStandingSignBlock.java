package slimeknights.mantle.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import slimeknights.mantle.block.entity.MantleSignBlockEntity;

public class MantleStandingSignBlock extends StandingSignBlock {
  public MantleStandingSignBlock(Properties props, WoodType type) {
    super(props, type);
  }

  @Override
  public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
    return new MantleSignBlockEntity(pPos, pState);
  }
}
