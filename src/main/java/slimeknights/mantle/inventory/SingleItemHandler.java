package slimeknights.mantle.inventory;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import slimeknights.mantle.tileentity.MantleTileEntity;

import javax.annotation.Nonnull;

/**
 * Item handler containing exactly one item.
 */
@SuppressWarnings("unused")
@RequiredArgsConstructor
public abstract class SingleItemHandler<T extends MantleTileEntity> implements IItemHandlerModifiable {
  protected final T parent;
  private final int maxStackSize;

  /** Current item in this slot */
  @Getter
  private ItemStack stack = ItemStack.EMPTY;

  /**
   * Sets the stack in this duct
   * @param newStack  New stack
   */
  public void setStack(ItemStack newStack) {
    this.stack = newStack;
    parent.setChangedFast();
  }

  /**
   * Checks if the given stack is valid for this slot
   * @param stack  Stack
   * @return  True if valid
   */
  protected abstract boolean isItemValid(ItemStack stack);


  /* Properties */

  @Override
  public boolean isItemValid(int slot, ItemStack stack) {
    return slot == 0 && isItemValid(stack);
  }

  @Override
  public int getSlots() {
    return 1;
  }

  @Override
  public int getSlotLimit(int slot) {
    return maxStackSize;
  }

  @Nonnull
  @Override
  public ItemStack getStackInSlot(int slot) {
    if (slot == 0) {
      return stack;
    }
    return ItemStack.EMPTY;
  }


  /* Interaction */

  @Override
  public void setStackInSlot(int slot, ItemStack stack) {
    if (slot == 0) {
      setStack(stack);
    }
  }
  
  @Nonnull
  @Override
  public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
    if (stack.isEmpty()) {
      return ItemStack.EMPTY;
    }
    if (slot == 0) {
      ItemStack current = getStack();
      if (current.isEmpty()) {
        if (this.isItemValid(slot, stack)) {
          // insert up to the stack limit
          int size = Math.min(stack.getCount(), getSlotLimit(0));
          if (!simulate) {
            this.setStack(ItemHandlerHelper.copyStackWithSize(stack, size));
          }
          return ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - size);
        }
      } else if (ItemHandlerHelper.canItemStacksStack(current, stack)) {
        // increase up to the stack limit
        int added = Math.min(stack.getCount(), getSlotLimit(0) - current.getCount());
        if (added > 0) {
          if (!simulate) {
            current.grow(added);
            setStack(current);
          }
          return ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - added);
        }
      }
    }
    return stack;
  }

  @Nonnull
  @Override
  public ItemStack extractItem(int slot, int amount, boolean simulate) {
    if (amount == 0 || slot != 0) {
      return ItemStack.EMPTY;
    }
    if (stack.isEmpty()) {
      return ItemStack.EMPTY;
    }

    // if amount is less than our size, need to do some shrinking
    if (amount < stack.getCount()) {
      ItemStack result = ItemHandlerHelper.copyStackWithSize(stack, amount);
      if (!simulate) {
        setStack(ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - amount));
      }
      return result;
    }
    // equal to or bigger means we give them our stack directly
    if (simulate) {
      return stack.copy();
    } else {
      ItemStack ret = stack;
      setStack(ItemStack.EMPTY);
      return ret;
    }
  }

  /**
   * Writes this module to NBT
   * @return  Module in NBT
   */
  public CompoundTag writeToNBT() {
    CompoundTag nbt = new CompoundTag();
    if (!stack.isEmpty()) {
      stack.save(nbt);
    }
    return nbt;
  }

  /**
   * Reads this module from NBT
   * @param nbt  NBT
   */
  public void readFromNBT(CompoundTag nbt) {
    stack = ItemStack.of(nbt);
  }
}
