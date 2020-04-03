package slimeknights.mantle.util;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import slimeknights.mantle.Mantle;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileEntityUtil {

  @Nullable
  public static <T extends TileEntity> T getTileEntity(@Nonnull Class<T> clazz, @Nullable IBlockReader world, @Nonnull BlockPos pos) {
    return getTileEntity(clazz, world, pos, false);
  }

  @Nullable
  public static <T extends TileEntity> T getTileEntity(@Nonnull Class<T> clazz, @Nullable IBlockReader world, @Nonnull BlockPos pos, boolean logWrongType) {
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

  public static boolean isBlockLoaded(@Nullable IBlockReader world, @Nonnull BlockPos pos) {
    if (world == null) {
      return false;
    }
    if (world instanceof IWorldReader) {
      return ((IWorldReader) world).isBlockLoaded(pos);
    }
    return true;
  }
}
