package slimeknights.mantle.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.INamedContainerProvider;
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
import javax.annotation.Nullable;

public abstract class InventoryBlock extends Block {

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
  @Override
  public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult rayTraceResult) {
    if (player.isSuppressingBounce()) {
      return ActionResultType.PASS;
    }

    if (!world.isRemote) {
      return this.openGui(player, world, pos) ? ActionResultType.SUCCESS : ActionResultType.PASS;
    }

    return ActionResultType.SUCCESS;
  }

  @Override
  public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
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
      TileEntity tileentity = worldIn.getTileEntity(pos);

      if (tileentity instanceof InventoryTileEntity) {
        dropInventoryItems(state, worldIn, pos, tileentity);

        worldIn.updateComparatorOutputLevel(pos, this);
      }
    }

    super.onReplaced(state, worldIn, pos, newState, isMoving);
  }

  protected void dropInventoryItems(BlockState state, World worldIn, BlockPos pos, TileEntity tileentity) {
    InventoryHelper.dropInventoryItems(worldIn, pos, (IInventory) tileentity);
  }

  @Override
  public boolean eventReceived(BlockState state, World worldIn, BlockPos pos, int id, int param) {
    super.eventReceived(state, worldIn, pos, id, param);
    TileEntity tileentity = worldIn.getTileEntity(pos);
    return tileentity != null && tileentity.receiveClientEvent(id, param);
  }

  @Override
  @Nullable
  public INamedContainerProvider getContainer(BlockState state, World worldIn, BlockPos pos) {
    TileEntity tileentity = worldIn.getTileEntity(pos);
    return tileentity instanceof INamedContainerProvider ? (INamedContainerProvider) tileentity : null;
  }
}
