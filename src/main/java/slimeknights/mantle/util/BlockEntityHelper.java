package slimeknights.mantle.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import slimeknights.mantle.Mantle;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Utilities to help in handling of tile entities
 */
@SuppressWarnings("WeakerAccess")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BlockEntityHelper {
  /**
   * Gets a tile entity if present and the right type
   * @param clazz  Tile entity class
   * @param world  World instance
   * @param pos    Tile entity position
   * @param <T>    Tile entity type
   * @return  Optional of the tile entity, empty if missing or wrong class
   */
  public static <T> Optional<T> get(Class<T> clazz, @Nullable BlockGetter world, BlockPos pos) {
    return get(clazz, world, pos, false);
  }

  /**
   * Gets a tile entity if present and the right type
   * @param clazz         Tile entity class
   * @param world         World instance
   * @param pos           Tile entity position
   * @param logWrongType  If true, logs a warning if the type is wrong
   * @param <T>    Tile entity type
   * @return  Optional of the tile entity, empty if missing or wrong class
   */
  public static <T> Optional<T> get(Class<T> clazz, @Nullable BlockGetter world, BlockPos pos, boolean logWrongType) {
    if (!isBlockLoaded(world, pos)) {
      return Optional.empty();
    }

    //TODO: This causes freezes if being called from onLoad
    BlockEntity tile = world.getBlockEntity(pos);
    if (tile == null) {
      return Optional.empty();
    }

    if (clazz.isInstance(tile)) {
      return Optional.of(clazz.cast(tile));
    } else if (logWrongType) {
      Mantle.logger.warn("Unexpected TileEntity class at {}, expected {}, but found: {}", pos, clazz, tile.getClass());
    }

    return Optional.empty();
  }

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
