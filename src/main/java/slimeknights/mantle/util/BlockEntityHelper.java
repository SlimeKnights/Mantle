package slimeknights.mantle.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;

import javax.annotation.Nullable;

/**
 * Utilities to help in handling of tile entities
 */
@SuppressWarnings("WeakerAccess")
public class BlockEntityHelper {
  private BlockEntityHelper() {}

  /**
   * Checks if the given block is loaded
   * @param world  World instance
   * @param pos    Position to check
   * @return  True if its loaded
   */
  @SuppressWarnings("deprecation")
  public static boolean isBlockLoaded(@Nullable BlockGetter world, BlockPos pos) {
    if (world == null) {
      return false;
    }
    if (world instanceof LevelReader) {
      return ((LevelReader) world).hasChunkAt(pos);
    }
    return true;
  }

  /**
   * Gets a block entity, checking its chunk is loaded first via {@link #isBlockLoaded(BlockGetter, BlockPos)}.
   * @see slimeknights.mantle.network.packet.BlockEntityPacket#getBlockEntity(BlockGetter, BlockPos, Object)
   */
  @SuppressWarnings("unused")
  @Nullable
  public static BlockEntity getLoaded(@Nullable BlockGetter world, BlockPos pos) {
    if (isBlockLoaded(world, pos)) {
      return world.getBlockEntity(pos);
    }
    return null;
  }

  /** Handles the unchecked cast for a block entity ticker */
  @SuppressWarnings("unchecked")
  @Nullable
  public static <HAVE extends BlockEntity, RET extends BlockEntity> BlockEntityTicker<RET> castTicker(BlockEntityType<RET> expected, BlockEntityType<HAVE> have, BlockEntityTicker<? super HAVE> ticker) {
    return have == expected ? (BlockEntityTicker<RET>)ticker : null;
  }

  /** Handles the unchecked cast for a block entity ticker */
  @Nullable
  public static <HAVE extends BlockEntity, RET extends BlockEntity> BlockEntityTicker<RET> serverTicker(Level level, BlockEntityType<RET> expected, BlockEntityType<HAVE> have, BlockEntityTicker<? super HAVE> ticker) {
    return level.isClientSide ? null : castTicker(expected, have, ticker);
  }
}
