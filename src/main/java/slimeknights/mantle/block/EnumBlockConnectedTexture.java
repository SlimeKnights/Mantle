package slimeknights.mantle.block;

import javax.annotation.Nonnull;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

 /**
  * Creates a block with textures that connect to other blocks
  * <p>
  * Based off a tutorial by Darkhax, used under the Creative Commons Zero 1.0 Universal license
  */
public class EnumBlockConnectedTexture<E extends Enum<E> & EnumBlock.IEnumMeta & IStringSerializable> extends EnumBlock<E> {
    
    public EnumBlockConnectedTexture(Material material, PropertyEnum<E> prop, Class<E> clazz) {
      super(material, prop, clazz);
      
      // By default none of the sides are connected
      this.setDefaultState(this.blockState.getBaseState()
                                          .withProperty(BlockConnectedTexture.CONNECTED_DOWN, Boolean.FALSE)
                                          .withProperty(BlockConnectedTexture.CONNECTED_EAST, Boolean.FALSE)
                                          .withProperty(BlockConnectedTexture.CONNECTED_NORTH, Boolean.FALSE)
                                          .withProperty(BlockConnectedTexture.CONNECTED_SOUTH, Boolean.FALSE)
                                          .withProperty(BlockConnectedTexture.CONNECTED_UP, Boolean.FALSE)
                                          .withProperty(BlockConnectedTexture.CONNECTED_WEST, Boolean.FALSE));
    }
    
    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos position) {
      // Creates the state to use for the block. This is where we check if every side is
      // connectable or not.
      return state.withProperty(BlockConnectedTexture.CONNECTED_DOWN,  this.isSideConnectable(state, world, position, EnumFacing.DOWN))
                  .withProperty(BlockConnectedTexture.CONNECTED_EAST,  this.isSideConnectable(state, world, position, EnumFacing.EAST))
                  .withProperty(BlockConnectedTexture.CONNECTED_NORTH, this.isSideConnectable(state, world, position, EnumFacing.NORTH))
                  .withProperty(BlockConnectedTexture.CONNECTED_SOUTH, this.isSideConnectable(state, world, position, EnumFacing.SOUTH))
                  .withProperty(BlockConnectedTexture.CONNECTED_UP,    this.isSideConnectable(state, world, position, EnumFacing.UP))
                  .withProperty(BlockConnectedTexture.CONNECTED_WEST,  this.isSideConnectable(state, world, position, EnumFacing.WEST));
    }
    
    @Nonnull
    @Override
    protected BlockStateContainer createBlockState() {
      if(prop == null) {
        return new BlockStateContainer(this, new IProperty[] { tmp, BlockConnectedTexture.CONNECTED_DOWN, BlockConnectedTexture.CONNECTED_UP, BlockConnectedTexture.CONNECTED_NORTH, BlockConnectedTexture.CONNECTED_SOUTH, BlockConnectedTexture.CONNECTED_WEST, BlockConnectedTexture.CONNECTED_EAST });
      }
      return new BlockStateContainer(this, new IProperty[] { prop, BlockConnectedTexture.CONNECTED_DOWN, BlockConnectedTexture.CONNECTED_UP, BlockConnectedTexture.CONNECTED_NORTH, BlockConnectedTexture.CONNECTED_SOUTH, BlockConnectedTexture.CONNECTED_WEST, BlockConnectedTexture.CONNECTED_EAST });
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
    private boolean isSideConnectable(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
      final IBlockState connected = world.getBlockState(pos.offset(side));
      return connected != null && canConnect(state, connected);
    }
    
    /**
     * Checks if this block should connect to another block
     * @param state BlockState to check
     * @return True if the block is valid to connect
     */
    protected boolean canConnect(IBlockState original, IBlockState connected) {
      return connected.getBlock() == original.getBlock() && connected.getValue(prop) == original.getValue(prop);
    }
}