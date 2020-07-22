package slimeknights.mantle.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

/**
 * Creates a block with textures that connect to other blocks
 * <p>
 * Based off a tutorial by Darkhax, used under the Creative Commons Zero 1.0 Universal license
 */
public class ConnectedTextureBlock extends Block {

  // These are the properties used for determining whether or not a side is connected. They
  // do NOT take up block IDs, they are unlisted properties
  public static final BooleanProperty CONNECTED_DOWN = BooleanProperty.create("connected_down");
  public static final BooleanProperty CONNECTED_UP = BooleanProperty.create("connected_up");
  public static final BooleanProperty CONNECTED_NORTH = BooleanProperty.create("connected_north");
  public static final BooleanProperty CONNECTED_SOUTH = BooleanProperty.create("connected_south");
  public static final BooleanProperty CONNECTED_WEST = BooleanProperty.create("connected_west");
  public static final BooleanProperty CONNECTED_EAST = BooleanProperty.create("connected_east");

  public ConnectedTextureBlock(Block.Properties builder) {

    super(builder);
    // By default none of the sides are connected
    this.setDefaultState(this.stateContainer.getBaseState()
            .with(CONNECTED_DOWN, Boolean.FALSE)
            .with(CONNECTED_EAST, Boolean.FALSE)
            .with(CONNECTED_NORTH, Boolean.FALSE)
            .with(CONNECTED_SOUTH, Boolean.FALSE)
            .with(CONNECTED_UP, Boolean.FALSE)
            .with(CONNECTED_WEST, Boolean.FALSE));
  }

  @Override
  public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {

    // Creates the state to use for the block. This is where we check if every side is
    // connectable or not.

    return stateIn.with(CONNECTED_DOWN, this.isSideConnectable(stateIn, worldIn, currentPos, Direction.DOWN))
            .with(CONNECTED_EAST, this.isSideConnectable(stateIn, worldIn, currentPos, Direction.EAST))
            .with(CONNECTED_NORTH, this.isSideConnectable(stateIn, worldIn, currentPos, Direction.NORTH))
            .with(CONNECTED_SOUTH, this.isSideConnectable(stateIn, worldIn, currentPos, Direction.SOUTH))
            .with(CONNECTED_UP, this.isSideConnectable(stateIn, worldIn, currentPos, Direction.UP))
            .with(CONNECTED_WEST, this.isSideConnectable(stateIn, worldIn, currentPos, Direction.WEST));
  }

  @Override
  protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
    builder.add(CONNECTED_DOWN, CONNECTED_UP, CONNECTED_NORTH, CONNECTED_SOUTH, CONNECTED_WEST, CONNECTED_EAST);
  }

  /**
   * Checks if a specific side of a block can connect to this block. For this example, a side
   * is connectable if the block is the same block as this one.
   *
   * @param state Base state to check
   * @param world The world to run the check in.
   * @param pos The position of the block to check for.
   * @param side The side of the block to check.
   * @return Whether or not the side is connectable.
   */
  private boolean isSideConnectable(BlockState state, IWorld world, BlockPos pos, Direction side) {
    return this.canConnect(state, world.getBlockState(pos.offset(side)));
  }

  /**
   * Checks if this block should connect to another block
   * @param original BlockState to check
   * @param connected BlockState to check
   * @return True if the block is valid to connect
   */
  protected boolean canConnect(BlockState original, BlockState connected) {
    return original.getBlock() == connected.getBlock();
  }
}
