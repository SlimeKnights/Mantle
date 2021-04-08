package slimeknights.mantle.inventory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

// TODO: cleanup
public class MultiModuleContainer<TILE extends BlockEntity> extends BaseContainer<TILE> {

  public List<ScreenHandler> subContainers = Lists.newArrayList();

  // lookup used to redirect slot specific things to the appropriate container
  protected Map<Integer, ScreenHandler> slotContainerMap = Maps.newHashMap();
  protected Map<ScreenHandler, Pair<Integer, Integer>> subContainerSlotRanges = Maps.newHashMap();
  protected int subContainerSlotStart = -1;
  protected Set<ScreenHandler> shiftClickContainers = Sets.newHashSet();

  public MultiModuleContainer(ScreenHandlerType<?> containerType, int id, @Nullable PlayerInventory inv, @Nullable TILE tile) {
    super(containerType, id, inv, tile);
  }

  /**
   * @param subcontainer        The container to add
   * @param preferForShiftClick If true shift clicking on slots of the main-container will try to move to this module before the player inventory
   */
  public void addSubContainer(ScreenHandler subcontainer, boolean preferForShiftClick) {
    if (this.subContainers.isEmpty()) {
      this.subContainerSlotStart = this.slots.size();
    }
    this.subContainers.add(subcontainer);

    if (preferForShiftClick) {
      this.shiftClickContainers.add(subcontainer);
    }

    int begin = this.slots.size();
    for (Object slot : subcontainer.slots) {
      WrapperSlot wrapper = new WrapperSlot((Slot) slot);
      this.addSlot(wrapper);
      this.slotContainerMap.put(wrapper.id, subcontainer);
    }
    int end = this.slots.size();
    this.subContainerSlotRanges.put(subcontainer, Pair.of(begin, end));
  }

  @Nullable
  public <CONTAINER extends ScreenHandler> CONTAINER getSubContainer(Class<CONTAINER> clazz) {
    return this.getSubContainer(clazz, 0);
  }

  @Nullable
  public <CONTAINER extends ScreenHandler> CONTAINER getSubContainer(Class<CONTAINER> clazz, int index) {
    for (ScreenHandler sub : this.subContainers) {
      if (clazz.isAssignableFrom(sub.getClass())) {
        index--;
      }

      if (index < 0) {
        return clazz.cast(sub);
      }
    }

    return null;
  }

  public ScreenHandler getSlotContainer(int slotNumber) {
    if (this.slotContainerMap.containsKey(slotNumber)) {
      return this.slotContainerMap.get(slotNumber);
    }

    return this;
  }

  @Override
  public boolean canUse(PlayerEntity playerIn) {
    // check if subcontainers are valid
    for (ScreenHandler sub : this.subContainers) {
      if (!sub.canUse(playerIn)) {
        return false;
      }
    }

    return super.canUse(playerIn);
  }

  @Override
  public void close(PlayerEntity playerIn) {
    for (ScreenHandler sub : this.subContainers) {
      sub.close(playerIn);
    }

    super.close(playerIn);
  }

  @Override
  public ItemStack onSlotClick(int slotId, int dragType, SlotActionType type, PlayerEntity player) {
    if (slotId == -999 && type == SlotActionType.QUICK_CRAFT) {
      for (ScreenHandler container : this.subContainers) {
        container.onSlotClick(slotId, dragType, type, player);
      }
    }

    return super.onSlotClick(slotId, dragType, type, player);
  }

  // More sophisticated version of the one in BaseContainer
  // Takes submodules into account when shiftclicking!
  @Override
  public ItemStack transferSlot(PlayerEntity playerIn, int index) {
    Slot slot = this.slots.get(index);

    if (slot == null || !slot.hasStack()) {
      return ItemStack.EMPTY;
    }

    ItemStack ret = slot.getStack().copy();
    ItemStack itemstack = slot.getStack().copy();

    ScreenHandler container = this.getSlotContainer(index);
    boolean nothingDone = true;

    // Is the slot from a module?
    if (container != this) {
      // Try moving module -> tile inventory
      nothingDone &= this.moveToTileInventory(itemstack);

      // Try moving module -> player inventory
      nothingDone &= this.moveToPlayerInventory(itemstack);
    }
    // Is the slot from the tile?
    else if (index < this.subContainerSlotStart || (index < this.playerInventoryStart && this.subContainerSlotStart < 0)) {
      // Try moving tile -> preferred modules
      nothingDone &= this.refillAnyContainer(itemstack, this.subContainers);

      // Try moving module -> player inventory
      nothingDone &= this.moveToPlayerInventory(itemstack);

      // Try moving module -> all submodules
      nothingDone &= this.moveToAnyContainer(itemstack, this.subContainers);
    }
    // Slot is from the player inventory (if present)
    else if (index >= this.playerInventoryStart && this.playerInventoryStart >= 0) {
      // Try moving player -> tile inventory
      nothingDone &= this.moveToTileInventory(itemstack);

      // try moving player -> modules
      nothingDone &= this.moveToAnyContainer(itemstack, this.subContainers);
    }
    // you violated some assumption or something. Shame on you.
    else {
      return ItemStack.EMPTY;
    }

    if (nothingDone) {
      return ItemStack.EMPTY;
    }

    return this.notifySlotAfterTransfer(playerIn, itemstack, ret, slot);
  }

  protected ItemStack notifySlotAfterTransfer(PlayerEntity player, ItemStack stack, ItemStack original, Slot slot) {
    // notify slot
    slot.onStackChanged(stack, original);

    if (stack.getCount() == original.getCount()) {
      return ItemStack.EMPTY;
    }

    // update slot we pulled from
    slot.setStack(stack);
    slot.onTakeItem(player, stack);

    if (slot.hasStack() && slot.getStack().isEmpty()) {
      slot.setStack(ItemStack.EMPTY);
    }

    return original;
  }

  protected boolean moveToTileInventory(ItemStack itemstack) {
    if (itemstack.isEmpty()) {
      return false;
    }

    int end = this.subContainerSlotStart;
    if (end < 0) {
      end = this.playerInventoryStart;
    }
    return !this.insertItem(itemstack, 0, end, false);
  }

  protected boolean moveToPlayerInventory(ItemStack itemstack) {
    if (itemstack.isEmpty()) {
      return false;
    }

    return this.playerInventoryStart > 0 && !this.insertItem(itemstack, this.playerInventoryStart, this.slots.size(), true);
  }

  protected boolean moveToAnyContainer(ItemStack itemstack, Collection<ScreenHandler> containers) {
    if (itemstack.isEmpty()) {
      return false;
    }

    for (ScreenHandler submodule : containers) {
      if (this.moveToContainer(itemstack, submodule)) {
        return true;
      }
    }

    return false;
  }

  protected boolean moveToContainer(ItemStack itemstack, ScreenHandler container) {
    Pair<Integer, Integer> range = this.subContainerSlotRanges.get(container);
    return !this.insertItem(itemstack, range.getLeft(), range.getRight(), false);
  }

  protected boolean refillAnyContainer(ItemStack itemstack, Collection<ScreenHandler> containers) {
    if (itemstack.isEmpty()) {
      return false;
    }

    for (ScreenHandler submodule : containers) {
      if (this.refillContainer(itemstack, submodule)) {
        return true;
      }
    }

    return false;
  }

  protected boolean refillContainer(ItemStack itemstack, ScreenHandler container) {
    Pair<Integer, Integer> range = this.subContainerSlotRanges.get(container);
    return !this.mergeItemStackRefill(itemstack, range.getLeft(), range.getRight(), false);
  }

  /**
   * Searches for a sidechest to display in the UI
   */
  @Nullable
  public <TE extends BlockEntity> TE detectTE(Class<TE> clazz) {
    if (this.tile == null) {
      return null;
    } else {
      return ObjectUtils.firstNonNull(this.detectChest(this.tile.getPos().north(), clazz),
        this.detectChest(this.tile.getPos().east(), clazz),
        this.detectChest(this.tile.getPos().south(), clazz),
        this.detectChest(this.tile.getPos().west(), clazz));
    }
  }

  @Nullable
  private <TE extends BlockEntity> TE detectChest(BlockPos pos, Class<TE> clazz) {
    if (this.tile == null) {
      return null;
    } else {
      if (this.tile.getWorld() == null) {
        return null;
      } else {
        BlockEntity te = this.tile.getWorld().getBlockEntity(pos);
        if (clazz.isInstance(te)) {
          return clazz.cast(te);
        }
      }
    }

    return null;
  }
}
