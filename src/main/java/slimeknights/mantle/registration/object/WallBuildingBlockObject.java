package slimeknights.mantle.registration.object;

import net.minecraft.block.Block;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.WallBlock;
import net.minecraftforge.registries.IRegistryDelegate;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

@SuppressWarnings({"WeakerAccess", "unused"})
public class WallBuildingBlockObject extends BuildingBlockObject {
  private final Supplier<? extends WallBlock> wall;

  /**
   * Creates a new building block object with all parameters
   * @param block    Block supplier
   * @param slab     Slab supplier
   * @param stairs   Stairs supplier
   * @param wall     Wall supplier
   */
  public WallBuildingBlockObject(Supplier<? extends Block> block, Supplier<? extends SlabBlock> slab,
      Supplier<? extends StairsBlock> stairs, Supplier<? extends WallBlock> wall) {
    super(block, slab, stairs);
    this.wall = wall;
  }

  /**
   * Creates a new object from a building block object plus a wall
   * @param object  Previous building block object
   * @param wall    Wall object
   */
  public WallBuildingBlockObject(BuildingBlockObject object, Supplier<? extends WallBlock> wall) {
    super(object);
    this.wall = wall;
  }

  /**
   * Creates a new wall building block object from the given blocks
   * @param object  Building block object
   * @param wall    Wall object
   * @return  BuildingBlockObject instance
   */
  public static WallBuildingBlockObject fromBlocks(BuildingBlockObject object, Block wall) {
    IRegistryDelegate<Block> wallDelegate = wall.delegate;
    return new WallBuildingBlockObject(object, () -> (WallBlock) wallDelegate.get());
  }

  /**
   * Creates a new wall building block object from the given blocks
   * @param block    Block object
   * @param slab     Slab object
   * @param stairs   Stairs object
   * @param wall     Wall object
   * @return  BuildingBlockObject instance
   */
  public static WallBuildingBlockObject fromBlocks(Block block, Block slab, Block stairs, Block wall) {
    return fromBlocks(BuildingBlockObject.fromBlocks(block, slab, stairs), wall);
  }

  /** Gets the wall for this block */
  public WallBlock getWall() {
    return wall.get();
  }

  @Override
  public List<Block> values() {
    return Arrays.asList(get(), getSlab(), getStairs(), getWall());
  }
}
