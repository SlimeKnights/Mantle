package slimeknights.mantle.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import slimeknights.mantle.Mantle;

import javax.annotation.Nullable;

/**
 * Utilities to help in handling of tile entities
 */
@SuppressWarnings("WeakerAccess")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TileEntityHelper {

  /**
   * Gets a tile entity if present and the right type
   * @param clazz  Tile entity class
   * @param world  World instance
   * @param pos    Tile entity position
   * @param <T>    Tile entity type
   * @return  Tile entity if it is the type, null if missing or wrong class
   */
  @Nullable
  public static <T> T getTileEntity(Class<T> clazz, @Nullable IBlockReader world, BlockPos pos) {
    return getTileEntity(clazz, world, pos, false);
  }

  /**
   * Gets a tile entity if present and the right type
   * @param clazz         Tile entity class
   * @param world         World instance
   * @param pos           Tile entity position
   * @param logWrongType  If true, logs a warning if the type is wrong
   * @param <T>    Tile entity type
   * @return  Tile entity if it is the type, null if missing or wrong class
   */
  @Nullable
  public static <T> T getTileEntity(Class<T> clazz, @Nullable IBlockReader world, BlockPos pos, boolean logWrongType) {
    if (!isBlockLoaded(world, pos)) {
      return null;
    }

    //TODO: This causes freezes if being called from onLoad
    TileEntity tile = world.getTileEntity(pos);
    if (tile == null) {
      return null;
    }

    if (clazz.isInstance(tile)) {
      return clazz.cast(tile);
    } else if (logWrongType) {
      Mantle.logger.warn("Unexpected TileEntity class at {}, expected {}, but found: {}", pos, clazz, tile.getClass());
    }

    return null;
  }

  /**
   * Checks if the given block is loaded
   * @param world  World instance
   * @param pos    Position to check
   * @return  True if its loaded
   */
  public static boolean isBlockLoaded(@Nullable IBlockReader world, BlockPos pos) {
    if (world == null) {
      return false;
    }
    if (world instanceof IWorldReader) {
      return ((IWorldReader) world).isBlockLoaded(pos);
    }
    return true;
  }
}
