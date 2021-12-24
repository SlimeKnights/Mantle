package slimeknights.mantle.inventory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

// TODO: cleanup
public class MultiModuleContainerMenu<TILE extends BlockEntity> extends BaseContainerMenu<TILE> {

  public List<AbstractContainerMenu> subContainers = Lists.newArrayList();

  // lookup used to redirect slot specific things to the appropriate container
  protected Map<Integer, AbstractContainerMenu> slotContainerMap = Maps.newHashMap();
  protected Map<AbstractContainerMenu, Pair<Integer, Integer>> subContainerSlotRanges = Maps.newHashMap();
  protected int subContainerSlotStart = -1;
  protected Set<AbstractContainerMenu> shiftClickContainers = Sets.newHashSet();

  public MultiModuleContainerMenu(MenuType<?> containerType, int id, @Nullable Inventory inv, @Nullable TILE tile) {
    super(containerType, id, inv, tile);
  }

  /**
   * @param subcontainer        The container to add
   * @param preferForShiftClick If true shift clicking on slots of the main-container will try to move to this module before the player inventory
   */
  public void addSubContainer(AbstractContainerMenu subcontainer, boolean preferForShiftClick) {
    if (this.subContainers.isEmpty()) {
      this.subContainerSlotStart = this.slots.size();
    }
    this.subContainers.add(subcontainer);

    if (preferForShiftClick) {
      this.shiftClickContainers.add(subcontainer);
    }

    int begin = this.slots.size();
    for (Slot slot : subcontainer.slots) {
      WrapperSlot wrapper = new WrapperSlot(slot);
      this.addSlot(wrapper);
      this.slotContainerMap.put(wrapper.index, subcontainer);
    }
    int end = this.slots.size();
    this.subContainerSlotRanges.put(subcontainer, Pair.of(begin, end));
  }

  @Nullable
  public <CONTAINER extends AbstractContainerMenu> CONTAINER getSubContainer(Class<CONTAINER> clazz) {
    return this.getSubContainer(clazz, 0);
  }

  @Nullable
  public <CONTAINER extends AbstractContainerMenu> CONTAINER getSubContainer(Class<CONTAINER> clazz, int index) {
    for (AbstractContainerMenu sub : this.subContainers) {
      if (clazz.isAssignableFrom(sub.getClass())) {
        index--;
      }

      if (index < 0) {
        return clazz.cast(sub);
      }
    }

    return null;
  }

  public AbstractContainerMenu getSlotContainer(int slotNumber) {
    if (this.slotContainerMap.containsKey(slotNumber)) {
      return this.slotContainerMap.get(slotNumber);
    }

    return this;
  }

  @Override
  public boolean stillValid(Player playerIn) {
    // check if subcontainers are valid
    for (AbstractContainerMenu sub : this.subContainers) {
      if (!sub.stillValid(playerIn)) {
        return false;
      }
    }

    return super.stillValid(playerIn);
  }

  @Override
  public void removed(Player playerIn) {
    for (AbstractContainerMenu sub : this.subContainers) {
      sub.removed(playerIn);
    }

    super.removed(playerIn);
  }

  @Override
  public void clicked(int slotId, int dragType, ClickType type, Player player) {
    if (slotId == -999 && type == ClickType.QUICK_CRAFT) {
      for (AbstractContainerMenu container : this.subContainers) {
        container.clicked(slotId, dragType, type, player);
      }
    }
    super.clicked(slotId, dragType, type, player);
  }

  // More sophisticated version of the one in BaseContainer
  // Takes submodules into account when shiftclicking!
  @Override
  public ItemStack quickMoveStack(Player playerIn, int index) {
    Slot slot = this.slots.get(index);

    if (!slot.hasItem()) {
      return ItemStack.EMPTY;
    }

    ItemStack ret = slot.getItem().copy();
    ItemStack itemstack = slot.getItem().copy();

    AbstractContainerMenu container = this.getSlotContainer(index);
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

  protected ItemStack notifySlotAfterTransfer(Player player, ItemStack stack, ItemStack original, Slot slot) {
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

  protected boolean moveToAnyContainer(ItemStack itemstack, Collection<AbstractContainerMenu> containers) {
    if (itemstack.isEmpty()) {
      return false;
    }

    for (AbstractContainerMenu submodule : containers) {
      if (this.moveToContainer(itemstack, submodule)) {
        return true;
      }
    }

    return false;
  }

  protected boolean moveToContainer(ItemStack itemstack, AbstractContainerMenu container) {
    Pair<Integer, Integer> range = this.subContainerSlotRanges.get(container);
    return !this.moveItemStackTo(itemstack, range.getLeft(), range.getRight(), false);
  }

  protected boolean refillAnyContainer(ItemStack itemstack, Collection<AbstractContainerMenu> containers) {
    if (itemstack.isEmpty()) {
      return false;
    }

    for (AbstractContainerMenu submodule : containers) {
      if (this.refillContainer(itemstack, submodule)) {
        return true;
      }
    }

    return false;
  }

  protected boolean refillContainer(ItemStack itemstack, AbstractContainerMenu container) {
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
      return ObjectUtils.firstNonNull(this.detectChest(this.tile.getBlockPos().north(), clazz),
        this.detectChest(this.tile.getBlockPos().east(), clazz),
        this.detectChest(this.tile.getBlockPos().south(), clazz),
        this.detectChest(this.tile.getBlockPos().west(), clazz));
    }
  }

  @Nullable
  private <TE extends BlockEntity> TE detectChest(BlockPos pos, Class<TE> clazz) {
    if (this.tile == null) {
      return null;
    } else {
      if (this.tile.getLevel() == null) {
        return null;
      } else {
        BlockEntity te = this.tile.getLevel().getBlockEntity(pos);
        if (clazz.isInstance(te)) {
          return clazz.cast(te);
        }
      }
    }

    return null;
  }
}
