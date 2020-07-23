package slimeknights.mantle.registration.object;

import net.minecraft.block.Block;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraftforge.registries.IRegistryDelegate;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * Object containing a block
 */
@SuppressWarnings("WeakerAccess")
public class BuildingBlockObject extends BlockItemObject<Block> {
  private final Supplier<? extends SlabBlock> slab;
  private final Supplier<? extends StairsBlock> stairs;

  /**
   * Creates a new object from a set of suppliers
   * @param block   Base block
   * @param slab    Slab block
   * @param stairs  Stairs block
   */
  public BuildingBlockObject(Supplier<? extends Block> block, Supplier<? extends SlabBlock> slab, Supplier<? extends StairsBlock> stairs) {
    super(block);
    this.slab = slab;
    this.stairs = stairs;
  }

  /**
   * Creates a new object from another building block object
   * @param object   Object to copy
   */
  protected BuildingBlockObject(BuildingBlockObject object) {
    super(object.block);
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
    IRegistryDelegate<Block> slabDelegate = slab.delegate;
    IRegistryDelegate<Block> stairsDelegate = stairs.delegate;
    return new BuildingBlockObject(
      block.delegate,
      () -> (SlabBlock) slabDelegate.get(),
      () -> (StairsBlock) stairsDelegate.get()
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
