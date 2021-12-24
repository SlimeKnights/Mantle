package slimeknights.mantle.registration.object;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceBlock;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import static slimeknights.mantle.registration.RegistrationHelper.castDelegate;

/**
 * Object containing a block with slab, stairs, and fence variants
 */
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
   * Creates a new object from a building block object plus a fence entry
   * @param object  Previous building block object
   * @param fence   Fence entry
   */
  public FenceBuildingBlockObject(BuildingBlockObject object, Block fence) {
    this(object, castDelegate(fence.delegate));
  }

  /** Gets the fence for this block */
  public FenceBlock getFence() {
    return Objects.requireNonNull(fence.get(), "Fence Building Block Object missing fence");
  }

  @Override
  public List<Block> values() {
    return Arrays.asList(get(), getSlab(), getStairs(), getFence());
  }
}
