package slimeknights.mantle.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import slimeknights.mantle.inventory.BaseContainerMenu;

import javax.annotation.Nullable;

/**
 * Base class for blocks with an inventory. Can be used with any block that is {@link MenuProvider} and exposes an {@link Capabilities.ItemHandler#BLOCK} capability.
 * @see slimeknights.mantle.block.entity.InventoryBlockEntity
 */
@SuppressWarnings("WeakerAccess")
public abstract class InventoryBlock extends Block implements EntityBlock {

  protected InventoryBlock(BlockBehaviour.Properties builder) {
    super(builder);
  }


  /* UI */

  /**
   * Called when the block is activated to open the UI. Override to return {@link InteractionResult#PASS} for blocks with no UI
   * @param player Player instance
   * @param world  World instance
   * @param pos    Block position
   * @return {@link InteractionResult#CONSUME} on opening the container, or {@link InteractionResult#SUCCESS} client side if opening is expected.
   *        {@link InteractionResult#PASS} if no container is opened, should be done both sides.
   */
  protected InteractionResult openGui(BlockState state, Level world, BlockPos pos, Player player) {
    if (!world.isClientSide()) {
      MenuProvider container = this.getMenuProvider(state, world, pos);
      if (container != null) {
        player.openMenu(container, pos);
        if (player.containerMenu instanceof BaseContainerMenu<?> menu && player instanceof ServerPlayer serverPlayer) {
          menu.syncOnOpen(serverPlayer);
        }
      }
      return InteractionResult.CONSUME;
    }
    return InteractionResult.SUCCESS;
  }

  @Override
  protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
    return this.openGui(state, level, pos, player);
  }

  @Override
  @Nullable
  public MenuProvider getMenuProvider(BlockState state, Level worldIn, BlockPos pos) {
    return worldIn.getBlockEntity(pos) instanceof MenuProvider menu ? menu : null;
  }


  /* Inventory handling */

  @Override
  public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
    if (state.getBlock() != newState.getBlock()) {
      IItemHandler inventory = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
      if (inventory != null) {
        dropInventoryItems(state, level, pos, inventory);
        level.updateNeighbourForOutputSignal(pos, this);
      }
    }
    super.onRemove(state, level, pos, newState, isMoving);
  }

  /**
   * Called when the block is replaced to drop contained items.
   * @param state       Block state
   * @param worldIn     Tile world
   * @param pos         Tile position
   * @param inventory   Item handler
   */
  @SuppressWarnings("unused") // API
  protected void dropInventoryItems(BlockState state, Level worldIn, BlockPos pos, IItemHandler inventory) {
    dropInventoryItems(worldIn, pos, inventory);
  }

  /**
   * Drops all items from the given inventory in world
   * @param world      World instance
   * @param pos        Position to drop
   * @param inventory  Inventory instance
   */
  public static void dropInventoryItems(Level world, BlockPos pos, IItemHandler inventory) {
    double x = pos.getX();
    double y = pos.getY();
    double z = pos.getZ();
    for(int i = 0; i < inventory.getSlots(); ++i) {
      Containers.dropItemStack(world, x, y, z, inventory.getStackInSlot(i));
    }
  }

  @Override
  public boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
    super.triggerEvent(state, level, pos, id, param);
    BlockEntity be = level.getBlockEntity(pos);
    return be != null && be.triggerEvent(id, param);
  }
}
