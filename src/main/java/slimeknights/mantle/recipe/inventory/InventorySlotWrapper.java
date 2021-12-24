package slimeknights.mantle.recipe.inventory;

import lombok.AllArgsConstructor;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

/**
 * Implementation of {@link ISingleItemInventory} to wrap another {@link IInventory}
 */
@AllArgsConstructor
public class InventorySlotWrapper implements ISingleItemInventory {
  private final Container parent;
  private final int index;

  @Override
  public ItemStack getStack() {
    return parent.getItem(index);
  }
}
