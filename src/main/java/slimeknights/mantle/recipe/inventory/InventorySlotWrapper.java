package slimeknights.mantle.recipe.inventory;

import lombok.AllArgsConstructor;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

/**
 * Implementation of {@link ISingleItemInventory} to wrap another {@link Inventory}
 */
@AllArgsConstructor
public class InventorySlotWrapper implements ISingleItemInventory {
  private final Inventory parent;
  private final int index;

  @Override
  public ItemStack getStack() {
    return parent.getStack(index);
  }
}
