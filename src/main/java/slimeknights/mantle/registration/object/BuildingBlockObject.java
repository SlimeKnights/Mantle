package slimeknights.mantle.registration.object;

import lombok.Getter;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.List;

/**
 * Object containing a block with slab and stairs variants
 */
@SuppressWarnings("WeakerAccess")
@Getter
public class BuildingBlockObject extends ItemObject<Block,Block> implements MultiObject.Holders<Block> {
  protected final Holder<Block> slabHolder;
  protected final Holder<Block> stairsHolder;

  /**
   * Creates a new building block object from three blocks
   * @param block   Base block
   * @param slab    Slab block, should be an instance of SlabBlock
   * @param stairs  Stairs block, should be an instance of StairBlock
   */
  public BuildingBlockObject(Holder<Block> block, Holder<Block> slab, Holder<Block> stairs) {
    super(block);
    this.slabHolder = slab;
    this.stairsHolder = stairs;
  }

  /**
   * Creates a new object from a ItemObject and some suppliers.
   * @param block   Base block
   * @param slab    Slab block
   * @param stairs  Stairs block
   */
  public BuildingBlockObject(DeferredHolder<Block,? extends Block> block, Holder<Block> slab, Holder<Block> stairs) {
    super(block);
    this.slabHolder = slab;
    this.stairsHolder = stairs;
  }

  /**
   * Creates a new object from another building block object, intended to be used in subclasses to copy properties
   * @param object   Object to copy
   */
  protected BuildingBlockObject(BuildingBlockObject object) {
    super(object);
    this.slabHolder = object.slabHolder;
    this.stairsHolder = object.stairsHolder;
  }

  /** Gets the slab for this block */
  public SlabBlock getSlab() {
    return (SlabBlock) slabHolder.value();
  }

  /** Gets the stairs for this block */
  public StairBlock getStairs() {
    return (StairBlock) stairsHolder.value();
  }

  @Override
  public List<Holder<Block>> holders() {
    return List.of(holder, slabHolder, stairsHolder);
  }
}
