package slimeknights.mantle.recipe.inventory;

import lombok.AllArgsConstructor;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

/**
 * Implementation of {@link ISingleItemInventory} to wrap another {@link IInventory}
 */
@AllArgsConstructor
public class InventorySlotWrapper implements ISingleItemInventory {
  private final IInventory parent;
  private final int index;

  @Override
  public ItemStack getStack() {
    return parent.getStackInSlot(index);
  }
}
