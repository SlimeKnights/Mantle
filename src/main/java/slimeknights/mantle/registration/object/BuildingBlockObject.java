package slimeknights.mantle.registration.object;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import slimeknights.mantle.registration.RegistrationHelper;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Object containing a block with slab and stairs variants
 */
@SuppressWarnings("WeakerAccess")
public class BuildingBlockObject extends ItemObject<Block> {
  private final Supplier<? extends SlabBlock> slab;
  private final Supplier<? extends StairBlock> stairs;

  /**
   * Creates a new building block object from three blocks
   * @param block   Base block
   * @param slab    Slab block, should be an instance of SlabBlock
   * @param stairs  Stairs block, should be an instance of StairsBlock
   */
  public BuildingBlockObject(Block block, Block slab, Block stairs) {
    super(BuiltInRegistries.BLOCK, block);
    this.slab = RegistrationHelper.getCastedHolder(BuiltInRegistries.BLOCK, slab);
    this.stairs = RegistrationHelper.getCastedHolder(BuiltInRegistries.BLOCK, stairs);
  }

  /**
   * Creates a new object from a ItemObject and some suppliers.
   * @param block   Base block
   * @param slab    Slab block
   * @param stairs  Stairs block
   */
  public BuildingBlockObject(ItemObject<? extends Block> block, Supplier<? extends SlabBlock> slab, Supplier<? extends StairBlock> stairs) {
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

  /** Gets the slab for this block */
  public SlabBlock getSlab() {
    return Objects.requireNonNull(slab.get(), "Building Block Object missing slab");
  }

  /** Gets the stairs for this block */
  public StairBlock getStairs() {
    return Objects.requireNonNull(stairs.get(), "Building Block Object missing stairs");
  }

  /**
   * Gets an array of the blocks in this object
   * @return  Array of the blocks in this object
   */
  public List<Block> values() {
    return Arrays.asList(get(), getSlab(), getStairs());
  }
}
