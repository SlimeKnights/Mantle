package slimeknights.mantle.inventory;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import slimeknights.mantle.util.TileEntityHelper;

import org.jetbrains.annotations.Nullable;

public class BaseContainer<TILE extends BlockEntity> extends ScreenHandler {

  public static double MAX_DISTANCE = 64;
  public static int BASE_Y_OFFSET = 84;

  @Nullable
  protected final TILE tile;

  @Nullable
  protected final PlayerInventory inv;

  protected BaseContainer(ScreenHandlerType<?> type, int id, @Nullable PlayerInventory inv, @Nullable TILE tile) {
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

      if (player.currentScreenHandler instanceof BaseContainer) {
        if (this.sameGui((BaseContainer) player.currentScreenHandler)) {
          this.syncWithOtherContainer((BaseContainer) player.currentScreenHandler, playerOpened);
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
  public boolean canUse(PlayerEntity playerIn) {
    if (this.tile == null) {
      return true;
    }

    if (!tile.isRemoved()) {
      //prevent Containers from remaining valid after the chunk has unloaded;
      World world = tile.getWorld();

      if (world == null) {
        return false;
      }

      return world.canSetBlock(tile.getPos());
    }

    return false;
  }

  @Override
  public DefaultedList<ItemStack> getStacks() {
    return super.getStacks();
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

    int start = this.slots.size();

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
      throw new IllegalStateException("BaseContainer: Player inventory has to be last slots. Add all slots before adding the player inventory.");
    }
    return super.addSlot(slotIn);
  }

  @Override
  public ItemStack transferSlot(PlayerEntity playerIn, int index) {
    // we can only support inventory <-> playerInventory
    if (this.playerInventoryStart < 0) {
      // so we don't do anything if no player inventory is present because we don't know what to do
      return ItemStack.EMPTY;
    }

    ItemStack itemstack = ItemStack.EMPTY;
    Slot slot = this.slots.get(index);

    // slot that was clicked on not empty?
    if (slot != null && slot.hasStack()) {
      ItemStack itemstack1 = slot.getStack();
      itemstack = itemstack1.copy();
      int end = this.slots.size();

      // Is it a slot in the main inventory? (aka not player inventory)
      if (index < this.playerInventoryStart) {
        // try to put it into the player inventory (if we have a player inventory)
        if (!this.insertItem(itemstack1, this.playerInventoryStart, end, true)) {
          return ItemStack.EMPTY;
        }
      }
      // Slot is in the player inventory (if it exists), transfer to main inventory
      else if (!this.insertItem(itemstack1, 0, this.playerInventoryStart, false)) {
        return ItemStack.EMPTY;
      }

      if (itemstack1.isEmpty()) {
        slot.setStack(ItemStack.EMPTY);
      } else {
        slot.markDirty();
      }
    }

    return itemstack;
  }

  // Fix for a vanilla bug: doesn't take Slot.getMaxStackSize into account
  @Override
  protected boolean insertItem(ItemStack stack, int startIndex, int endIndex, boolean useEndIndex) {
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
        slot = this.slots.get(k);
        itemstack1 = slot.getStack();

        if (!itemstack1.isEmpty() && itemstack1.getItem() == stack.getItem() && ItemStack.areTagsEqual(stack, itemstack1) && this.canInsertIntoSlot(stack, slot)) {
          int l = itemstack1.getCount() + stack.getCount();
          int limit = Math.min(stack.getMaxCount(), slot.getMaxItemCount(stack));

          if (l <= limit) {
            stack.setCount(0);
            itemstack1.setCount(l);
            slot.markDirty();
            flag1 = true;
          } else if (itemstack1.getCount() < limit) {
            stack.decrement(limit - itemstack1.getCount());
            itemstack1.setCount(limit);
            slot.markDirty();
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
      Slot slot = this.slots.get(k);
      ItemStack itemstack1 = slot.getStack();

      // Forge: Make sure to respect isItemValid in the slot.
      if (itemstack1.isEmpty() && slot.canInsert(stack) && this.canInsertIntoSlot(stack, slot)) {
        int limit = slot.getMaxItemCount(stack);
        ItemStack stack2 = stack.copy();

        if (stack2.getCount() > limit) {
          stack2.setCount(limit);
          stack.decrement(limit);
        } else {
          stack.setCount(0);
        }

        slot.setStack(stack2);
        slot.markDirty();
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

  /**
   * Gets a tile entity from a packet buffer
   * @param buf     Packet buffer instance
   * @param type    Tile entity class
   * @param <TILE>  Tile entity type
   * @return Tile entity, or null if unable to find
   */
  @Nullable
  public static <TILE extends BlockEntity> TILE getTileEntityFromBuf(@Nullable PacketByteBuf buf, Class<TILE> type) {
    if (buf == null) {
      return null;
    }
    return DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> () -> TileEntityHelper.getTile(type, MinecraftClient.getInstance().world, buf.readBlockPos()).orElse(null));
  }
}
