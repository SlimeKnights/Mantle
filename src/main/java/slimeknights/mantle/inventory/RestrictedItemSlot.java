package slimeknights.mantle.inventory;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class RestrictedItemSlot extends Slot {

  private final Item allowedItem;

  public RestrictedItemSlot(Item item, IInventory inventoryIn, int index, int xPosition, int yPosition) {
    super(inventoryIn, index, xPosition, yPosition);
    this.allowedItem = item;
  }

  @Override
  public boolean mayPlace(ItemStack stack) {
    return !stack.isEmpty() && stack.getItem() == this.allowedItem;
  }
}
