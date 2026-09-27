package slimeknights.mantle.recipe.input;

import lombok.AllArgsConstructor;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

/**
 * Implementation of {@link SingleItemInput} to wrap a {@link Container}
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
