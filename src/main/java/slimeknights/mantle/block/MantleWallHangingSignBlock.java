package slimeknights.mantle.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import slimeknights.mantle.block.entity.MantleHangingSignBlockEntity;

public class MantleWallHangingSignBlock extends WallHangingSignBlock {
  public MantleWallHangingSignBlock(Properties props, WoodType type) {
    super(type, props);
  }

  @Override
  public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
    return new MantleHangingSignBlockEntity(pPos, pState);
  }
}
