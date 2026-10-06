package slimeknights.mantle.registration.object;

import lombok.Getter;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WallBlock;

import java.util.List;

/**
 * Object containing a block with slab, stairs, and wall variants
 */
@SuppressWarnings("unused")
@Getter
public class WallBuildingBlockObject extends BuildingBlockObject {
  protected final Holder<Block> wallHolder;

  /**
   * Creates a new object from a building block object plus a wall.
   * @param object  Previous building block object
   * @param wall    Wall object
   */
  public WallBuildingBlockObject(BuildingBlockObject object, Holder<Block> wall) {
    super(object);
    this.wallHolder = wall;
  }

  /** Gets the wall for this block */
  public WallBlock getWall() {
    return (WallBlock) wallHolder.value();
  }

  @Override
  public List<Holder<Block>> holders() {
    return List.of(holder, slabHolder, stairsHolder, wallHolder);
  }
}
