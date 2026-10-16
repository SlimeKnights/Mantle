package slimeknights.mantle.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** @deprecated not difficult to implement, and most implementations will want {@link SmartItemHandlerSlot} */
@Deprecated(forRemoval = true)
public class RestrictedItemSlot extends Slot {

  private final Item allowedItem;

  public RestrictedItemSlot(Item item, Container inventoryIn, int index, int xPosition, int yPosition) {
    super(inventoryIn, index, xPosition, yPosition);
    this.allowedItem = item;
  }

  @Override
  public boolean mayPlace(ItemStack stack) {
    return !stack.isEmpty() && stack.getItem() == this.allowedItem;
  }
}
