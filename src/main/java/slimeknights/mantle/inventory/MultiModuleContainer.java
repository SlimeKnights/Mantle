package slimeknights.mantle.inventory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

// TODO: cleanup
public class MultiModuleContainer<TILE extends TileEntity> extends BaseContainer<TILE> {

  public List<Container> subContainers = Lists.newArrayList();

  // lookup used to redirect slot specific things to the appropriate container
  protected Map<Integer, Container> slotContainerMap = Maps.newHashMap();
  protected Map<Container, Pair<Integer, Integer>> subContainerSlotRanges = Maps.newHashMap();
  protected int subContainerSlotStart = -1;
  protected Set<Container> shiftClickContainers = Sets.newHashSet();

  public MultiModuleContainer(ContainerType<?> containerType, int id, @Nullable PlayerInventory inv, @Nullable TILE tile) {
    super(containerType, id, inv, tile);
  }

  /**
   * @param subcontainer        The container to add
   * @param preferForShiftClick If true shift clicking on slots of the main-container will try to move to this module before the player inventory
   */
  public void addSubContainer(Container subcontainer, boolean preferForShiftClick) {
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
      this.slotContainerMap.put(wrapper.index, subcontainer);
    }
    int end = this.slots.size();
    this.subContainerSlotRanges.put(subcontainer, Pair.of(begin, end));
  }

  @Nullable
  public <CONTAINER extends Container> CONTAINER getSubContainer(Class<CONTAINER> clazz) {
    return this.getSubContainer(clazz, 0);
  }

  @Nullable
  public <CONTAINER extends Container> CONTAINER getSubContainer(Class<CONTAINER> clazz, int index) {
    for (Container sub : this.subContainers) {
      if (clazz.isAssignableFrom(sub.getClass())) {
        index--;
      }

      if (index < 0) {
        return clazz.cast(sub);
      }
    }

    return null;
  }

  public Container getSlotContainer(int slotNumber) {
    if (this.slotContainerMap.containsKey(slotNumber)) {
      return this.slotContainerMap.get(slotNumber);
    }

    return this;
  }

  @Override
  public boolean stillValid(PlayerEntity playerIn) {
    // check if subcontainers are valid
    for (Container sub : this.subContainers) {
      if (!sub.stillValid(playerIn)) {
        return false;
      }
    }

    return super.stillValid(playerIn);
  }

  @Override
  public void removed(PlayerEntity playerIn) {
    for (Container sub : this.subContainers) {
      sub.removed(playerIn);
    }

    super.removed(playerIn);
  }

  @Override
  public ItemStack clicked(int slotId, int dragType, ClickType type, PlayerEntity player) {
    if (slotId == -999 && type == ClickType.QUICK_CRAFT) {
      for (Container container : this.subContainers) {
        container.clicked(slotId, dragType, type, player);
      }
    }

    return super.clicked(slotId, dragType, type, player);
  }

  // More sophisticated version of the one in BaseContainer
  // Takes submodules into account when shiftclicking!
  @Override
  public ItemStack quickMoveStack(PlayerEntity playerIn, int index) {
    Slot slot = this.slots.get(index);

    if (slot == null || !slot.hasItem()) {
      return ItemStack.EMPTY;
    }

    ItemStack ret = slot.getItem().copy();
    ItemStack itemstack = slot.getItem().copy();

    Container container = this.getSlotContainer(index);
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
    slot.onQuickCraft(stack, original);

    if (stack.getCount() == original.getCount()) {
      return ItemStack.EMPTY;
    }

    // update slot we pulled from
    slot.set(stack);
    slot.onTake(player, stack);

    if (slot.hasItem() && slot.getItem().isEmpty()) {
      slot.set(ItemStack.EMPTY);
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
    return !this.moveItemStackTo(itemstack, 0, end, false);
  }

  protected boolean moveToPlayerInventory(ItemStack itemstack) {
    if (itemstack.isEmpty()) {
      return false;
    }

    return this.playerInventoryStart > 0 && !this.moveItemStackTo(itemstack, this.playerInventoryStart, this.slots.size(), true);
  }

  protected boolean moveToAnyContainer(ItemStack itemstack, Collection<Container> containers) {
    if (itemstack.isEmpty()) {
      return false;
    }

    for (Container submodule : containers) {
      if (this.moveToContainer(itemstack, submodule)) {
        return true;
      }
    }

    return false;
  }

  protected boolean moveToContainer(ItemStack itemstack, Container container) {
    Pair<Integer, Integer> range = this.subContainerSlotRanges.get(container);
    return !this.moveItemStackTo(itemstack, range.getLeft(), range.getRight(), false);
  }

  protected boolean refillAnyContainer(ItemStack itemstack, Collection<Container> containers) {
    if (itemstack.isEmpty()) {
      return false;
    }

    for (Container submodule : containers) {
      if (this.refillContainer(itemstack, submodule)) {
        return true;
      }
    }

    return false;
  }

  protected boolean refillContainer(ItemStack itemstack, Container container) {
    Pair<Integer, Integer> range = this.subContainerSlotRanges.get(container);
    return !this.mergeItemStackRefill(itemstack, range.getLeft(), range.getRight(), false);
  }

  /**
   * Searches for a sidechest to display in the UI
   */
  @Nullable
  public <TE extends TileEntity> TE detectTE(Class<TE> clazz) {
    if (this.tile == null) {
      return null;
    } else {
      return ObjectUtils.firstNonNull(this.detectChest(this.tile.getBlockPos().north(), clazz),
        this.detectChest(this.tile.getBlockPos().east(), clazz),
        this.detectChest(this.tile.getBlockPos().south(), clazz),
        this.detectChest(this.tile.getBlockPos().west(), clazz));
    }
  }

  @Nullable
  private <TE extends TileEntity> TE detectChest(BlockPos pos, Class<TE> clazz) {
    if (this.tile == null) {
      return null;
    } else {
      if (this.tile.getLevel() == null) {
        return null;
      } else {
        TileEntity te = this.tile.getLevel().getBlockEntity(pos);
        if (clazz.isInstance(te)) {
          return clazz.cast(te);
        }
      }
    }

    return null;
  }
}
