package slimeknights.mantle.registration.object;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Object containing a block with slab and stairs variants
 */
@SuppressWarnings("WeakerAccess")
public class BuildingBlockObject extends ItemObject<Block> implements MultiObject<Block> {
  private final Supplier<? extends SlabBlock> slab;
  private final Supplier<? extends StairBlock> stairs;

  /**
   * Creates a new building block object from three blocks
   * @param block   Base block
   * @param slab    Slab block, should be an instance of SlabBlock
   * @param stairs  Stairs block, should be an instance of StairBlock
   */
  public BuildingBlockObject(Block block, SlabBlock slab, StairBlock stairs) {
    super(BuiltInRegistries.BLOCK, block);
    this.slab = () -> slab;
    this.stairs = () -> stairs;
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

  @Override
  public List<Block> values() {
    return List.of(get(), getSlab(), getStairs());
  }

  @Override
  public void forEach(Consumer<? super Block> consumer) {
    consumer.accept(get());
    consumer.accept(getSlab());
    consumer.accept(getStairs());
  }
}
