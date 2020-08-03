package slimeknights.mantle.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import slimeknights.mantle.item.RetexturedBlockItem;
import slimeknights.mantle.tileentity.IRetexturedTileEntity;
import slimeknights.mantle.util.TileEntityUtil;

import javax.annotation.Nullable;

/**
 * Logic for a retexturable block. Use alongside {@link slimeknights.mantle.tileentity.IRetexturedTileEntity} and {@link slimeknights.mantle.item.RetexturedBlockItem}
 */
@SuppressWarnings("WeakerAccess")
public abstract class RetexturedBlock extends Block {
  public RetexturedBlock(Properties properties) {
    super(properties);
  }

  @Override
  public boolean hasTileEntity(BlockState state) {
    return true;
  }

  @Nullable
  @Override
  public abstract TileEntity createTileEntity(BlockState state, IBlockReader world);

  @Override
  public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
    super.onBlockPlacedBy(world, pos, state, placer, stack);
    updateTextureBlock(world, pos, stack);
  }

  @Override
  public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
    return getPickBlock(world, pos, state);
  }


  /* Utils */

  /**
   * Call in {@link Block#onBlockPlacedBy(World, BlockPos, BlockState, LivingEntity, ItemStack)} to set the texture tag to the Tile Entity
   * @param world World where the block was placed
   * @param pos   Block position
   * @param stack Item stack
   */
  public static void updateTextureBlock(World world, BlockPos pos, ItemStack stack) {
    if (stack.hasTag()) {
      IRetexturedTileEntity te = TileEntityUtil.getTileEntity(IRetexturedTileEntity.class, world, pos);
      if (te != null) {
        te.updateTexture(RetexturedBlockItem.getTextureName(stack));
      }
    }
  }

  /**
   * Called in blocks to get the item stack for the current block
   * @param world World
   * @param pos   Pos
   * @param state State
   * @return Pickblock stack with proper NBT
   */
  public static ItemStack getPickBlock(IBlockReader world, BlockPos pos, BlockState state) {
    Block block = state.getBlock();
    ItemStack stack = new ItemStack(block);
    IRetexturedTileEntity te = TileEntityUtil.getTileEntity(IRetexturedTileEntity.class, world, pos);
    if (te != null) {
      RetexturedBlockItem.setTexture(stack, te.getTextureName());
    }
    return stack;
  }
}
