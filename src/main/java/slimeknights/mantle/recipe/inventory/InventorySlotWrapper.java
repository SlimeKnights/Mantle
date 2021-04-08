package slimeknights.mantle.recipe.inventory;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

/**
 * Implementation of {@link ISingleItemInventory} to wrap another {@link Inventory}
 */
public class InventorySlotWrapper implements ISingleItemInventory {
  private final Inventory parent;
  private final int index;

  public InventorySlotWrapper(Inventory parent, int index) {
    this.parent = parent;
    this.index = index;
  }

  @Override
  public ItemStack getStack() {
    return parent.getStack(index);
  }
}
