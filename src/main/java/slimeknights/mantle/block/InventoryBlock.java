package slimeknights.mantle.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ContainerBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import slimeknights.mantle.tileentity.InventoryTileEntity;

import javax.annotation.Nonnull;

public abstract class InventoryBlock extends ContainerBlock {

  protected InventoryBlock(Block.Properties builder) {
    super(builder);
  }

  // inventories usually need a tileEntity
  @Override
  public boolean hasTileEntity(BlockState state) {
    return true;
  }

  @Nonnull
  @Override
  public abstract TileEntity createTileEntity(BlockState state, IBlockReader world);

  /**
   * Called when the block is activated. Return true if a GUI is opened, false if the block has no GUI.
   */
  protected abstract boolean openGui(PlayerEntity player, World world, BlockPos pos);

  @Deprecated
  public ActionResultType func_225533_a_(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult rayTraceResult) {
    if (player.func_226272_bl_()) {
      return ActionResultType.PASS;
    }

    if (!world.isRemote) {
      return this.openGui(player, world, pos) ? ActionResultType.SUCCESS : ActionResultType.PASS;
    }

    return ActionResultType.SUCCESS;
  }

  @Override
  public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer,
          ItemStack stack) {
    super.onBlockPlacedBy(worldIn, pos, state, placer, stack);

    // set custom name from named stack
    if (stack.hasDisplayName()) {
      TileEntity tileentity = worldIn.getTileEntity(pos);

      if (tileentity instanceof InventoryTileEntity) {
        ((InventoryTileEntity) tileentity).setCustomName(stack.getDisplayName());
      }
    }
  }

  @Override
  public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
    if (state.getBlock() != newState.getBlock()) {
      super.onReplaced(state, worldIn, pos, newState, isMoving);

      TileEntity tileentity = worldIn.getTileEntity(pos);

      if (tileentity instanceof InventoryTileEntity) {
        InventoryHelper.dropInventoryItems(worldIn, pos, (IInventory) tileentity);
        worldIn.updateComparatorOutputLevel(pos, this);
      }
    }
  }

  // BlockContainer sets this to invisible
  // we need model for standard forge rendering
  @Nonnull
  @Override
  public BlockRenderType getRenderType(BlockState state) {
    return BlockRenderType.MODEL;
  }
}