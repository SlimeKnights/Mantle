package slimeknights.mantle.inventory;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class RestrictedItemSlot extends Slot {

  private final Item allowedItem;

  public RestrictedItemSlot(Item item, Inventory inventoryIn, int index, int xPosition, int yPosition) {
    super(inventoryIn, index, xPosition, yPosition);
    this.allowedItem = item;
  }

  @Override
  public boolean canInsert(ItemStack stack) {
    return !stack.isEmpty() && stack.getItem() == this.allowedItem;
  }
}
