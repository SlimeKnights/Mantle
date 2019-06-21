package slimeknights.mantle.inventory;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class SlotRestrictedItem extends Slot {

  private final Item allowedItem;

  public SlotRestrictedItem(Item item, IInventory inventoryIn, int index, int xPosition, int yPosition) {
    super(inventoryIn, index, xPosition, yPosition);
    allowedItem = item;
  }

  @Override
  public boolean isItemValid(@Nonnull ItemStack stack) {
    return !stack.isEmpty() && stack.getItem() == allowedItem;
  }
}
