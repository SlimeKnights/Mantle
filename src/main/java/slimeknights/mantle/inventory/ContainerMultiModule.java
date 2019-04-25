package slimeknights.mantle.inventory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

public class ContainerMultiModule<T extends TileEntity & IInventory> extends BaseContainer<T> {

  public List<Container> subContainers = Lists.newArrayList();

  // lookup used to redirect slot specific things to the appropriate container
  protected Map<Integer, Container> slotContainerMap = Maps.newHashMap();
  protected Map<Container, Pair<Integer, Integer>> subContainerSlotRanges = Maps.newHashMap();
  protected int subContainerSlotStart = -1;
  protected Set<Container> shiftClickContainers = Sets.newHashSet();

  public ContainerMultiModule(T tile) {
    super(tile);
  }


  /**
   * @param subcontainer        The container to add
   * @param preferForShiftClick If true shift clicking on slots of the main-container will try to move to this module before the player inventory
   */
  public void addSubContainer(Container subcontainer, boolean preferForShiftClick) {
    if(subContainers.isEmpty()) {
      subContainerSlotStart = inventorySlots.size();
    }
    subContainers.add(subcontainer);

    if(preferForShiftClick) {
      shiftClickContainers.add(subcontainer);
    }

    int begin = inventorySlots.size();
    for(Object slot : subcontainer.inventorySlots) {
      SlotWrapper wrapper = new SlotWrapper((Slot) slot);
      addSlot(wrapper);
      slotContainerMap.put(wrapper.slotNumber, subcontainer);
    }
    int end = inventorySlots.size();
    subContainerSlotRanges.put(subcontainer, Pair.of(begin, end));
  }

  public <TC extends Container> TC getSubContainer(Class<TC> clazz) {
    return getSubContainer(clazz, 0);
  }

  public <TC extends Container> TC getSubContainer(Class<TC> clazz, int index) {
    for(Container sub : subContainers) {
      if(clazz.isAssignableFrom(sub.getClass())) {
        index--;
      }
      if(index < 0) {
        return (TC) sub;
      }
    }

    return null;
  }

  public Container getSlotContainer(int slotNumber) {
    if(slotContainerMap.containsKey(slotNumber)) {
      return slotContainerMap.get(slotNumber);
    }

    return this;
  }

  @Override
  public boolean canInteractWith(@Nonnull EntityPlayer playerIn) {
    // check if subcontainers are valid
    for(Container sub : subContainers) {
      if(!sub.canInteractWith(playerIn)) {
        return false;
      }
    }

    return super.canInteractWith(playerIn);
  }


  @Override
  public void onContainerClosed(EntityPlayer playerIn) {
    for(Container sub : subContainers) {
      sub.onContainerClosed(playerIn);
    }

    super.onContainerClosed(playerIn);
  }

  @Nonnull
  @Override
  public ItemStack slotClick(int slotId, int dragType, ClickType type, EntityPlayer player) {
    if(slotId == -999 && type == ClickType.QUICK_CRAFT) {
      for(Container container : subContainers) {
        container.slotClick(slotId, dragType, type, player);
      }
    }

    return super.slotClick(slotId, dragType, type, player);
  }

  // More sophisticated version of the one in BaseContainer
  // Takes submodules into account when shiftclicking!
  @Nonnull
  @Override
  public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
    Slot slot = this.inventorySlots.get(index);

    if(slot == null || !slot.getHasStack()) {
      return ItemStack.EMPTY;
    }

    ItemStack ret = slot.getStack().copy();
    ItemStack itemstack = slot.getStack().copy();

    Container container = getSlotContainer(index);
    boolean nothingDone = true;

    // Is the slot from a module?
    if(container != this) {
      // Try moving module -> tile inventory
      nothingDone &= moveToTileInventory(itemstack);

      // Try moving module -> player inventory
      nothingDone &= moveToPlayerInventory(itemstack);
    }
    // Is the slot from the tile?
    else if(index < subContainerSlotStart || (index < playerInventoryStart && subContainerSlotStart < 0)) {
      // Try moving tile -> preferred modules
      nothingDone &= refillAnyContainer(itemstack, subContainers);

      // Try moving module -> player inventory
      nothingDone &= moveToPlayerInventory(itemstack);

      // Try moving module -> all submodules
      nothingDone &= moveToAnyContainer(itemstack, subContainers);
    }
    // Slot is from the player inventory (if present)
    else if(index >= playerInventoryStart && playerInventoryStart >= 0) {
      // Try moving player -> tile inventory
      nothingDone &= moveToTileInventory(itemstack);

      // try moving player -> modules
      nothingDone &= moveToAnyContainer(itemstack, subContainers);
    }
    // you violated some assumption or something. Shame on you.
    else {
      return ItemStack.EMPTY;
    }

    if(nothingDone) {
      return ItemStack.EMPTY;
    }

    return notifySlotAfterTransfer(playerIn, itemstack, ret, slot);
  }

  @Nonnull
  protected ItemStack notifySlotAfterTransfer(EntityPlayer player, @Nonnull ItemStack stack, @Nonnull ItemStack original, Slot slot) {
    // notify slot
    slot.onSlotChange(stack, original);

    if(stack.getCount() == original.getCount()) {
      return ItemStack.EMPTY;
    }

    // update slot we pulled from
    slot.putStack(stack);
    slot.onTake(player, stack);

    if(slot.getHasStack() && slot.getStack().isEmpty()) {
      slot.putStack(ItemStack.EMPTY);
    }

    return original;
  }

  protected boolean moveToTileInventory(@Nonnull ItemStack itemstack) {
    if(itemstack.isEmpty()) {
      return false;
    }

    int end = subContainerSlotStart;
    if(end < 0) {
      end = playerInventoryStart;
    }
    return !this.mergeItemStack(itemstack, 0, end, false);
  }

  protected boolean moveToPlayerInventory(@Nonnull ItemStack itemstack) {
    if(itemstack.isEmpty()) {
      return false;
    }

    return playerInventoryStart > 0 && !this
        .mergeItemStack(itemstack, playerInventoryStart, this.inventorySlots.size(), true);
  }

  protected boolean moveToAnyContainer(@Nonnull ItemStack itemstack, Collection<Container> containers) {
    if(itemstack.isEmpty()) {
      return false;
    }

    for(Container submodule : containers) {
      if(moveToContainer(itemstack, submodule)) {
        return true;
      }
    }

    return false;
  }

  protected boolean moveToContainer(@Nonnull ItemStack itemstack, Container container) {
    Pair<Integer, Integer> range = subContainerSlotRanges.get(container);
    return !this.mergeItemStack(itemstack, range.getLeft(), range.getRight(), false);
  }


  protected boolean refillAnyContainer(@Nonnull ItemStack itemstack, Collection<Container> containers) {
    if(itemstack.isEmpty()) {
      return false;
    }

    for(Container submodule : containers) {
      if(refillContainer(itemstack, submodule)) {
        return true;
      }
    }

    return false;
  }

  protected boolean refillContainer(@Nonnull ItemStack itemstack, Container container) {
    Pair<Integer, Integer> range = subContainerSlotRanges.get(container);
    return !this.mergeItemStackRefill(itemstack, range.getLeft(), range.getRight(), false);
  }

  /** Searches for a sidechest to display in the UI */
  public <TE extends TileEntity> TE detectTE(Class<TE> clazz) {
    return ObjectUtils.firstNonNull(detectChest(this.pos.north(), clazz),
                                    detectChest(this.pos.east(), clazz),
                                    detectChest(this.pos.south(), clazz),
                                    detectChest(this.pos.west(), clazz));
  }

  private <TE extends TileEntity> TE detectChest(BlockPos pos, Class<TE> clazz) {
    TileEntity te = this.world.getTileEntity(pos);

    if(te != null && clazz.isAssignableFrom(te.getClass())) {
      return (TE) te;
    }
    return null;
  }
}
