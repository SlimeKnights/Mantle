package slimeknights.mantle.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import slimeknights.mantle.item.RetexturedBlockItem;
import slimeknights.mantle.tileentity.IRetexturedTileEntity;
import slimeknights.mantle.util.TileEntityHelper;

import javax.annotation.Nullable;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

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
  public abstract BlockEntity createTileEntity(BlockState state, BlockGetter world);

  @Override
  public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
    super.setPlacedBy(world, pos, state, placer, stack);
    updateTextureBlock(world, pos, stack);
  }

  @Override
  public ItemStack getPickBlock(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
    return getPickBlock(world, pos, state);
  }


  /* Utils */

  /**
   * Call in {@link Block#onBlockPlacedBy(World, BlockPos, BlockState, LivingEntity, ItemStack)} to set the texture tag to the Tile Entity
   * @param world World where the block was placed
   * @param pos   Block position
   * @param stack Item stack
   */
  public static void updateTextureBlock(Level world, BlockPos pos, ItemStack stack) {
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
  public static ItemStack getPickBlock(BlockGetter world, BlockPos pos, BlockState state) {
    Block block = state.getBlock();
    ItemStack stack = new ItemStack(block);
    TileEntityHelper.getTile(IRetexturedTileEntity.class, world, pos).ifPresent(te -> RetexturedBlockItem.setTexture(stack, te.getTextureName()));
    return stack;
  }
}
