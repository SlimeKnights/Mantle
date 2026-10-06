package slimeknights.mantle.registration.object;

import lombok.Getter;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceBlock;

import java.util.List;

/**
 * Object containing a block with slab, stairs, and fence variants
 */
@SuppressWarnings("unused")
@Getter
public class FenceBuildingBlockObject extends BuildingBlockObject {
  protected final Holder<Block> fenceHolder;

  /**
   * Creates a new object from a building block object plus a fence.
   * @param object  Previous building block object
   * @param fence   Fence object
   */
  public FenceBuildingBlockObject(BuildingBlockObject object, Holder<Block> fence) {
    super(object);
    this.fenceHolder = fence;
  }

  /** Gets the fence for this block */
  public FenceBlock getFence() {
    return (FenceBlock) fenceHolder.value();
  }

  @Override
  public List<Holder<Block>> holders() {
    return List.of(holder, slabHolder, stairsHolder, fenceHolder);
  }
}
