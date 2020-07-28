package slimeknights.mantle.registration.object;

import net.minecraft.block.Block;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import static slimeknights.mantle.registration.RegistrationHelper.castDelegate;

/**
 * Object containing a block
 */
@SuppressWarnings("WeakerAccess")
public class BuildingBlockObject extends ItemObject<Block> {
  private final Supplier<? extends SlabBlock> slab;
  private final Supplier<? extends StairsBlock> stairs;

  /**
   * Standard supplier method, expects the supplier to have resolved in order to call registry name.
   * @param block   Base block
   * @param slab    Slab block
   * @param stairs  Stairs block
   */
  protected BuildingBlockObject(Supplier<? extends Block> block, Supplier<? extends SlabBlock> slab, Supplier<? extends StairsBlock> stairs) {
    super(block);
    this.slab = slab;
    this.stairs = stairs;
  }

  /**
   * Creates a new object from a ItemObject. Avoids an extra supplier wrapper
   * @param block   Base block
   * @param slab    Slab block
   * @param stairs  Stairs block
   */
  public BuildingBlockObject(ItemObject<? extends Block> block, Supplier<? extends SlabBlock> slab, Supplier<? extends StairsBlock> stairs) {
    super(block);
    this.slab = slab;
    this.stairs = stairs;
  }

  /**
   * Creates a new object from another building block object, intended to be used in subclasses to copy properties
   * @param object   Object to copy
   */
  protected BuildingBlockObject(BuildingBlockObject object) {
    super(object);
    this.slab = object.slab;
    this.stairs = object.stairs;
  }

  /**
   * Creates a building block object from the set of blocks
   * @param block   Block
   * @param slab    Slab block
   * @param stairs  Stairs block
   * @return  BuildingBlockObject instance
   */
  public static BuildingBlockObject fromBlocks(Block block, Block slab, Block stairs) {
    return new BuildingBlockObject(
      block.delegate,
      castDelegate(slab.delegate),
      castDelegate(stairs.delegate)
    );
  }

  /** Gets the slab for this block */
  public SlabBlock getSlab() {
    return slab.get();
  }

  /** Gets the stairs for this block */
  public StairsBlock getStairs() {
    return stairs.get();
  }

  /**
   * Gets an array of the blocks in this object
   * @return  Array of the blocks in this object
   */
  public List<Block> values() {
    return Arrays.asList(get(), getSlab(), getStairs());
  }
}
