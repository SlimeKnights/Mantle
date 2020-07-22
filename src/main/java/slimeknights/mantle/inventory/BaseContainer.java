package slimeknights.mantle.inventory;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import slimeknights.mantle.util.SlimeknightException;
import slimeknights.mantle.util.TileEntityUtil;

import javax.annotation.Nullable;

public class BaseContainer<TILE extends TileEntity> extends Container {

  public static double MAX_DISTANCE = 64;
  public static int BASE_Y_OFFSET = 84;

  @Nullable
  protected final TILE tile;

  @Nullable
  protected final PlayerInventory inv;

  protected BaseContainer(ContainerType<?> type, int id, @Nullable PlayerInventory inv, @Nullable TILE tile) {
    super(type, id);
    this.inv = inv;
    this.tile = tile;
  }

  @Nullable
  public TILE getTile() {
    return this.tile;
  }

  public void syncOnOpen(ServerPlayerEntity playerOpened) {
    // find another player that already has the gui for this tile open
    ServerWorld server = playerOpened.getServerWorld();

    for (PlayerEntity player : server.getPlayers()) {
      if (player == playerOpened) {
        continue;
      }

      if (player.openContainer instanceof BaseContainer) {
        if (this.sameGui((BaseContainer) player.openContainer)) {
          this.syncWithOtherContainer((BaseContainer) player.openContainer, playerOpened);
          return;
        }
      }
    }

    // no player has a container open for the tile
    this.syncNewContainer(playerOpened);
  }

  /**
   * Called when the container is opened and another player already has a container for this tile open
   * Sync to the same state here.
   */
  protected void syncWithOtherContainer(BaseContainer otherContainer, ServerPlayerEntity player) {
  }

  /**
   * Called when the container is opened and no other player has it open.
   * Set the default state here.
   */
  protected void syncNewContainer(ServerPlayerEntity player) {
  }

  public boolean sameGui(BaseContainer otherContainer) {
    if (this.tile == null) {
      return false;
    }

    return this.tile == otherContainer.tile;
  }

  @Override
  public boolean canInteractWith(PlayerEntity playerIn) {
    if (this.tile == null) {
      return true;
    }

    if (!tile.isRemoved()) {
      //prevent Containers from remaining valid after the chunk has unloaded;
      World world = tile.getWorld();

      if (world == null) {
        return false;
      }

      return world.isBlockPresent(tile.getPos());
    }

    return false;
  }

  @Override
  public NonNullList<ItemStack> getInventory() {
    return super.getInventory();
  }

  /*
   * Call this to add the player's inventory to the GUI.
   */
  protected void addInventorySlots() {
    if (this.inv != null) {
      this.addInventorySlots(this.inv);
    }
  }

  protected int playerInventoryStart = -1;

  /*
   * Override this to set the X offset for the inventory slots.
   */
  protected int getInventoryXOffset() {
    return 8;
  }

  /*
  * Override this to set the Y offset for the inventory slots.
   */
  protected int getInventoryYOffset() {
    return BASE_Y_OFFSET;
  }

  protected void addInventorySlots(PlayerInventory inv) {
    int yOffset = this.getInventoryYOffset();
    int xOffset = this.getInventoryXOffset();

    int start = this.inventorySlots.size();

    for (int slotY = 0; slotY < 3; slotY++) {
      for (int slotX = 0; slotX < 9; slotX++) {
        addSlot(new Slot(inv, slotX + slotY * 9 + 9, xOffset + slotX * 18, yOffset + slotY * 18));
      }
    }

    yOffset += 58;
    for (int slotY = 0; slotY < 9; slotY++) {
      addSlot(new Slot(inv, slotY, xOffset + slotY * 18, yOffset));
    }

    this.playerInventoryStart = start;
  }

  @Override
  protected Slot addSlot(Slot slotIn) {
    if (this.playerInventoryStart >= 0) {
      throw new SlimeknightException("BaseContainer: Player inventory has to be last slots. Add all slots before adding the player inventory.");
    }
    return super.addSlot(slotIn);
  }

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
      } else {
        slot.onSlotChanged();
      }
    }

    return itemstack;
  }

  // Fix for a vanilla bug: doesn't take Slot.getMaxStackSize into account
  @Override
  protected boolean mergeItemStack(ItemStack stack, int startIndex, int endIndex, boolean useEndIndex) {
    boolean ret = this.mergeItemStackRefill(stack, startIndex, endIndex, useEndIndex);
    if (!stack.isEmpty() && stack.getCount() > 0) {
      ret |= this.mergeItemStackMove(stack, startIndex, endIndex, useEndIndex);
    }
    return ret;
  }

  // only refills items that are already present
  protected boolean mergeItemStackRefill(ItemStack stack, int startIndex, int endIndex, boolean useEndIndex) {
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

        if (!itemstack1.isEmpty() && itemstack1.getItem() == stack.getItem() && ItemStack.areItemStackTagsEqual(stack, itemstack1) && this.canMergeSlot(stack, slot)) {
          int l = itemstack1.getCount() + stack.getCount();
          int limit = Math.min(stack.getMaxStackSize(), slot.getItemStackLimit(stack));

          if (l <= limit) {
            stack.setCount(0);
            itemstack1.setCount(l);
            slot.onSlotChanged();
            flag1 = true;
          } else if (itemstack1.getCount() < limit) {
            stack.shrink(limit - itemstack1.getCount());
            itemstack1.setCount(limit);
            slot.onSlotChanged();
            flag1 = true;
          }
        }

        if (useEndIndex) {
          --k;
        } else {
          ++k;
        }
      }
    }

    return flag1;
  }

  // only moves items into empty slots
  protected boolean mergeItemStackMove(ItemStack stack, int startIndex, int endIndex, boolean useEndIndex) {
    if (stack.getCount() <= 0) {
      return false;
    }

    boolean flag1 = false;
    int k;

    if (useEndIndex) {
      k = endIndex - 1;
    } else {
      k = startIndex;
    }

    while (!useEndIndex && k < endIndex || useEndIndex && k >= startIndex) {
      Slot slot = this.inventorySlots.get(k);
      ItemStack itemstack1 = slot.getStack();

      // Forge: Make sure to respect isItemValid in the slot.
      if (itemstack1.isEmpty() && slot.isItemValid(stack) && this.canMergeSlot(stack, slot)) {
        int limit = slot.getItemStackLimit(stack);
        ItemStack stack2 = stack.copy();

        if (stack2.getCount() > limit) {
          stack2.setCount(limit);
          stack.shrink(limit);
        } else {
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
      } else {
        ++k;
      }
    }

    return flag1;
  }

  @Nullable
  public static <TILE extends TileEntity> TILE getTileEntityFromBuf(@Nullable PacketBuffer buf, Class<TILE> type) {
    if (buf == null) {
      return null;
    }

    return DistExecutor.callWhenOn(Dist.CLIENT, () -> () -> TileEntityUtil.getTileEntity(type, Minecraft.getInstance().world, buf.readBlockPos()));
  }
}
