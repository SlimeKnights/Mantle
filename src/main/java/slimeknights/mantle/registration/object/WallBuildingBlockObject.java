package slimeknights.mantle.registration.object;

import net.minecraft.block.Block;
import net.minecraft.block.WallBlock;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Object containing a block with slab, stairs, and wall variants
 */
@SuppressWarnings("unused")
public class WallBuildingBlockObject extends BuildingBlockObject {
  private final WallBlock wall;

  /**
   * Creates a new object from a building block object plus a wall.
   * @param object  Previous building block object
   * @param wall    Wall object
   */
  public WallBuildingBlockObject(BuildingBlockObject object, WallBlock wall) {
    super(object);
    this.wall = wall;
  }

  /** Gets the wall for this block */
  public WallBlock getWall() {
    return Objects.requireNonNull(wall, "Wall Building Block Object missing wall");
  }

  @Override
  public List<Block> values() {
    return Arrays.asList(get(), getSlab(), getStairs(), getWall());
  }
}
