package slimeknights.mantle.recipe.container;

import lombok.AllArgsConstructor;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import slimeknights.mantle.recipe.SingleItemInput;

/**
 * Implementation of {@link SingleItemInput} to wrap another {@link Container}
 */
@AllArgsConstructor
public class InventorySlotWrapper implements SingleItemInput {
  private final Container parent;
  private final int index;

  @Override
  public ItemStack getItem() {
    return parent.getItem(index);
  }
}
