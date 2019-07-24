package slimeknights.mantle.inventory;

import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;
import slimeknights.mantle.util.SlimeknightException;

import javax.annotation.Nonnull;
import java.util.List;

/** Same as Container but provides some extra functionality to simplify things */
public abstract class BaseContainer<T extends TileEntity> extends Container {

  protected double maxDist = 8 * 8; // 8 blocks
  protected T tile;
  protected final Block originalBlock; // used to check if the block we interacted with got broken
  protected final BlockPos pos;
  protected final World world;
  protected final LazyOptional<IItemHandler> itemHandler;

  public List<Container> subContainers = Lists.newArrayList();

  public BaseContainer(ContainerType<?> containerType, int windowId, T tile) {
    this(containerType, windowId, tile, null);
  }

  public BaseContainer(ContainerType<?> containerType, int windowId, T tile, Direction invDir) {
    super(containerType, windowId);
    this.tile = tile;

    if (tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, invDir).isPresent()) {
      this.itemHandler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, invDir);
    }
    else {
      this.itemHandler = net.minecraftforge.common.util.LazyOptional.of(() -> new EmptyHandler());
    }

    this.world = tile.getWorld();
    this.pos = tile.getPos();
    this.originalBlock = this.world.getBlockState(this.pos).getBlock();
  }

  public void syncOnOpen(ServerPlayerEntity playerOpened) {
    // find another player that already has the gui for this tile open
    ServerWorld server = playerOpened.getServerWorld();
    for (PlayerEntity player : server.getPlayers()) {
      if (player == playerOpened) {
        continue;
      }
      if (player.openContainer instanceof BaseContainer) {
        if (this.sameGui((BaseContainer<T>) player.openContainer)) {
          this.syncWithOtherContainer((BaseContainer<T>) player.openContainer, playerOpened);
          return;
        }
      }
    }

    // no player has a container open for the tile
    this.syncNewContainer(playerOpened);
  }

  public T getTile() {
    return this.tile;
  }

  public IItemHandler getItemHandler() {
    return this.itemHandler.orElse(new EmptyHandler());
  }

  /**
   * Called when the container is opened and another player already has a container for this tile open
   * Sync to the same state here.
   */
  protected void syncWithOtherContainer(BaseContainer<T> otherContainer, ServerPlayerEntity player) {
  }

  /**
   * Called when the container is opened and no other player has it open.
   * Set the default state here.
   */
  protected void syncNewContainer(ServerPlayerEntity player) {
  }

  public boolean sameGui(BaseContainer otherContainer) {
    return this.tile == otherContainer.tile;
  }

  @Override
  public boolean canInteractWith(@Nonnull PlayerEntity playerIn) {
    Block block = this.world.getBlockState(this.pos).getBlock();
    // does the block we interacted with still exist?
    if (block == Blocks.AIR || block != this.originalBlock) {
      return false;
    }

    // too far away from block?
    return playerIn.getDistanceSq((double) this.pos.getX() + 0.5d,
            (double) this.pos.getY() + 0.5d,
            (double) this.pos.getZ() + 0.5d) <= this.maxDist;
  }

  @Nonnull
  @Override
  @SuppressWarnings("unchecked")
  public NonNullList<ItemStack> getInventory() {
    return super.getInventory();
  }

  /*public String getInventoryDisplayName() {
    INameable nameable = null;
    if (this.itemHandler.orElse(new EmptyHandler()) instanceof InvWrapper) {
      nameable = ((InvWrapper) this.itemHandler.orElse(new EmptyHandler())).getInv();
      // if the inventory doesn't have a name fall back to checking the tileentity
      if (nameable.getDisplayName() == null) {
        nameable = null;
      }
    }
    if(nameable == null && this.tile instanceof INameable) {
      nameable = (INameable) this.tile;
    }
    if (nameable != null) {
      ITextComponent textName = nameable.getDisplayName();
      return textName != null ? textName.getFormattedText() : nameable.getName().toString();
    }
    return null;
  }*/

  // standard yOffset calculation for chestlike inventories:
  // yOffset = (numRows - 4) * 18; (the -4 because of the 3 rows of inventory + 1 row of hotbar)

  protected int playerInventoryStart = -1;

  /**
   * Draws the player inventory starting at the given position
   *
   * @param playerInventory The players inventory
   * @param xCorner         Default Value: 8
   * @param yCorner         Default Value: (rows - 4) * 18 + 103
   */
  protected void addPlayerInventory(PlayerInventory playerInventory, int xCorner, int yCorner) {
    int index = 9;

    int start = this.inventorySlots.size();

    for (int row = 0; row < 3; row++) {
      for (int col = 0; col < 9; col++) {
        this.addSlot(new Slot(playerInventory, index, xCorner + col * 18, yCorner + row * 18));
        index++;
      }
    }

    index = 0;
    for (int col = 0; col < 9; col++) {
      this.addSlot(new Slot(playerInventory, index, xCorner + col * 18, yCorner + 58));
      index++;
    }

    this.playerInventoryStart = start;
  }

  @Nonnull
  @Override
  protected Slot addSlot(Slot slotIn) {
    if (this.playerInventoryStart >= 0) {
      throw new SlimeknightException("BaseContainer: Player inventory has to be last slots. Add all slots before adding the player inventory.");
    }
    return super.addSlot(slotIn);
  }

  @Nonnull
  @Override
  public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
    // we can only support inventory <-> playerInventory
    if (this.playerInventoryStart < 0) {
      // so we don't do anything if no player inventory is present because we don't know what to do
      return ItemStack.EMPTY;
    }

    ItemStack itemstack = ItemStack.EMPTY;
    Slot slot = this.inventorySlots.get(index);

    // slot that was clicked on not empty?
    if (slot != null && slot.getHasStack()) {
      ItemStack itemstack1 = slot.getStack();
      itemstack = itemstack1.copy();
      int end = this.inventorySlots.size();

      // Is it a slot in the main inventory? (aka not player inventory)
      if (index < this.playerInventoryStart) {
        // try to put it into the player inventory (if we have a player inventory)
        if (!this.mergeItemStack(itemstack1, this.playerInventoryStart, end, true)) {
          return ItemStack.EMPTY;
        }
      }
      // Slot is in the player inventory (if it exists), transfer to main inventory
      else if (!this.mergeItemStack(itemstack1, 0, this.playerInventoryStart, false)) {
        return ItemStack.EMPTY;
      }

      if (itemstack1.isEmpty()) {
        slot.putStack(ItemStack.EMPTY);
      }
      else {
        slot.onSlotChanged();
      }
    }

    return itemstack;
  }

  // Fix for a vanilla bug: doesn't take Slot.getMaxStackSize into account
  @Override
  protected boolean mergeItemStack(@Nonnull ItemStack stack, int startIndex, int endIndex, boolean useEndIndex) {
    boolean ret = this.mergeItemStackRefill(stack, startIndex, endIndex, useEndIndex);
    if (!stack.isEmpty() && stack.getCount() > 0) {
      ret |= this.mergeItemStackMove(stack, startIndex, endIndex, useEndIndex);
    }
    return ret;
  }

  // only refills items that are already present
  protected boolean mergeItemStackRefill(@Nonnull ItemStack stack, int startIndex, int endIndex, boolean useEndIndex) {
    if (stack.getCount() <= 0) {
      return false;
    }

    boolean flag1 = false;
    int k = startIndex;

    if (useEndIndex) {
      k = endIndex - 1;
    }

    Slot slot;
    ItemStack itemstack1;

    if (stack.isStackable()) {
      while (stack.getCount() > 0 && (!useEndIndex && k < endIndex || useEndIndex && k >= startIndex)) {
        slot = this.inventorySlots.get(k);
        itemstack1 = slot.getStack();

        if (!itemstack1.isEmpty()
                && itemstack1.getItem() == stack.getItem()
                && ItemStack.areItemStackTagsEqual(stack, itemstack1)
                && this.canMergeSlot(stack, slot)) {
          int l = itemstack1.getCount() + stack.getCount();
          int limit = Math.min(stack.getMaxStackSize(), slot.getItemStackLimit(stack));

          if (l <= limit) {
            stack.setCount(0);
            itemstack1.setCount(l);
            slot.onSlotChanged();
            flag1 = true;
          }
          else if (itemstack1.getCount() < limit) {
            stack.shrink(limit - itemstack1.getCount());
            itemstack1.setCount(limit);
            slot.onSlotChanged();
            flag1 = true;
          }
        }

        if (useEndIndex) {
          --k;
        }
        else {
          ++k;
        }
      }
    }

    return flag1;
  }

  // only moves items into empty slots
  protected boolean mergeItemStackMove(@Nonnull ItemStack stack, int startIndex, int endIndex, boolean useEndIndex) {
    if (stack.getCount() <= 0) {
      return false;
    }

    boolean flag1 = false;
    int k;

    if (useEndIndex) {
      k = endIndex - 1;
    }
    else {
      k = startIndex;
    }

    while (!useEndIndex && k < endIndex || useEndIndex && k >= startIndex) {
      Slot slot = this.inventorySlots.get(k);
      ItemStack itemstack1 = slot.getStack();

      if (itemstack1.isEmpty() && slot.isItemValid(stack) && this.canMergeSlot(stack, slot)) // Forge: Make sure to respect isItemValid in the slot.
      {
        int limit = slot.getItemStackLimit(stack);
        ItemStack stack2 = stack.copy();
        if (stack2.getCount() > limit) {
          stack2.setCount(limit);
          stack.shrink(limit);
        }
        else {
          stack.setCount(0);
        }
        slot.putStack(stack2);
        slot.onSlotChanged();
        flag1 = true;

        if (stack.isEmpty()) {
          break;
        }
      }

      if (useEndIndex) {
        --k;
      }
      else {
        ++k;
      }
    }

    return flag1;
  }

}
