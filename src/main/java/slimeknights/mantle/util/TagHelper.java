package slimeknights.mantle.util;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

/**
 * Helpers to aid in reading and writing of NBT
 */
@SuppressWarnings("unused")
public class TagHelper {

  private TagHelper() {
  }

  /**
   * Converts a block position to NBT
   * @param pos Position
   * @return NBT compound
   */
  public static CompoundTag writePos(BlockPos pos) {
    CompoundTag tag = new CompoundTag();
    tag.putInt("X", pos.getX());
    tag.putInt("Y", pos.getY());
    tag.putInt("Z", pos.getZ());
    return tag;
  }

  /**
   * Reads a block position from a given tag compound
   * @param tag Tag to read
   * @return BlockPos, or null if invalid
   */
  @Nullable
  public static BlockPos readPos(CompoundTag tag) {
    if (tag.contains("X", NbtType.NUMBER) && tag.contains("Y", NbtType.NUMBER) && tag.contains("Z", NbtType.NUMBER)) {
      return new BlockPos(tag.getInt("X"), tag.getInt("Y"), tag.getInt("Z"));
    }
    return null;
  }
}
