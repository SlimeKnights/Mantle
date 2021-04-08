package slimeknights.mantle.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import slimeknights.mantle.item.RetexturedBlockItem;
import slimeknights.mantle.tileentity.IRetexturedTileEntity;
import slimeknights.mantle.util.TileEntityHelper;

import org.jetbrains.annotations.Nullable;

/**
 * Logic for a retexturable block. Use alongside {@link slimeknights.mantle.tileentity.IRetexturedTileEntity} and {@link slimeknights.mantle.item.RetexturedBlockItem}
 */
@SuppressWarnings("WeakerAccess")
public abstract class RetexturedBlock extends Block {
  public RetexturedBlock(Settings properties) {
    super(properties);
  }

  @Override
  public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
    super.onPlaced(world, pos, state, placer, stack);
    updateTextureBlock(world, pos, stack);
  }

  /* Utils */

  /**
   * Call in {@link Block#onPlaced(World, BlockPos, BlockState, LivingEntity, ItemStack)} to set the texture tag to the Tile Entity
   * @param world World where the block was placed
   * @param pos   Block position
   * @param stack Item stack
   */
  public static void updateTextureBlock(World world, BlockPos pos, ItemStack stack) {
    if (stack.hasTag()) {
      TileEntityHelper.getTile(IRetexturedTileEntity.class, world, pos).ifPresent(te -> te.updateTexture(RetexturedBlockItem.getTextureName(stack)));
    }
  }

  /**
   * Called in blocks to get the item stack for the current block
   * @param world World
   * @param pos   Pos
   * @param state State
   * @return Pickblock stack with proper NBT
   */
  public static ItemStack getPickBlock(BlockView world, BlockPos pos, BlockState state) {
    Block block = state.getBlock();
    ItemStack stack = new ItemStack(block);
    TileEntityHelper.getTile(IRetexturedTileEntity.class, world, pos).ifPresent(te -> RetexturedBlockItem.setTexture(stack, te.getTextureName()));
    return stack;
  }
}
