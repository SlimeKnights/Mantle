package slimeknights.mantle.registration.object;

import net.minecraft.block.Block;
import net.minecraft.block.FenceBlock;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Object containing a block with slab, stairs, and fence variants
 */
@SuppressWarnings("unused")
public class FenceBuildingBlockObject extends BuildingBlockObject {
  private final FenceBlock fence;

  /**
   * Creates a new object from a building block object plus a fence.
   * @param object  Previous building block object
   * @param fence   Fence object
   */
  public FenceBuildingBlockObject(BuildingBlockObject object, FenceBlock fence) {
    super(object);
    this.fence = fence;
  }

  /** Gets the fence for this block */
  public FenceBlock getFence() {
    return Objects.requireNonNull(fence, "Fence Building Block Object missing fence");
  }

  @Override
  public List<Block> values() {
    return Arrays.asList(get(), getSlab(), getStairs(), getFence());
  }
}
