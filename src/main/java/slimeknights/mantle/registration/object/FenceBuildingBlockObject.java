package slimeknights.mantle.registration.object;

import net.minecraft.block.Block;
import net.minecraft.block.FenceBlock;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import static slimeknights.mantle.registration.RegistrationHelper.castDelegate;

@SuppressWarnings("unused")
public class FenceBuildingBlockObject extends BuildingBlockObject {
  private final Supplier<? extends FenceBlock> fence;

  /**
   * Creates a new object from a building block object plus a fence.
   * @param object  Previous building block object
   * @param fence   Fence object
   */
  public FenceBuildingBlockObject(BuildingBlockObject object, Supplier<? extends FenceBlock> fence) {
    super(object);
    this.fence = fence;
  }

  /**
   * Creates a new building block object from the given blocks
   * @param object  Building block object instance
   * @param fence   Fence variant
   * @return  BuildingBlockObject instance
   */
  public static FenceBuildingBlockObject fromBlocks(BuildingBlockObject object, Block fence) {
    return new FenceBuildingBlockObject(object, castDelegate(fence.delegate));
  }

  /**
   * Creates a new building block object from the given blocks
   * @param block   Block
   * @param slab    Slab variant
   * @param stairs  Stairs variant
   * @param fence   Fence variant
   * @return  BuildingBlockObject instance
   */
  public static FenceBuildingBlockObject fromBlocks(Block block, Block slab, Block stairs, Block fence) {
    return fromBlocks(BuildingBlockObject.fromBlocks(block, slab, stairs), fence);
  }

  /** Gets the fence for this block */
  public FenceBlock getFence() {
    return fence.get();
  }

  @Override
  public List<Block> values() {
    return Arrays.asList(get(), getSlab(), getStairs(), getFence());
  }
}
