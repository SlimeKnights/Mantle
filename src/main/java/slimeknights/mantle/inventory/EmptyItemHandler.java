package slimeknights.mantle.inventory;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

/**
 * Item handler that contains no items. Use similarly to {@link net.minecraftforge.fluids.capability.templates.EmptyFluidHandler}
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EmptyItemHandler implements IItemHandler {
  public static final EmptyItemHandler INSTANCE = new EmptyItemHandler();

  @Override
  public int getSlots() {
    return 0;
  }

  @Override
  public int getSlotLimit(int slot) {
    return 0;
  }

  @Nonnull
  @Override
  public ItemStack getStackInSlot(int slot) {
    return ItemStack.EMPTY;
  }

  @Override
  public boolean isItemValid(int slot, ItemStack stack) {
    return false;
  }

  @Nonnull
  @Override
  public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
    return stack;
  }

  @Nonnull
  @Override
  public ItemStack extractItem(int slot, int amount, boolean simulate) {
    return ItemStack.EMPTY;
  }
}
