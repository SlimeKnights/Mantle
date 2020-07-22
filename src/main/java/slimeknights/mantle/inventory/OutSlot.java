package slimeknights.mantle.inventory;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

/** Slot that can only be taken out of */
public class OutSlot extends Slot {

  public OutSlot(IInventory inventoryIn, int index, int xPosition, int yPosition) {
    super(inventoryIn, index, xPosition, yPosition);
  }

  @Override
  public boolean isItemValid(ItemStack stack) {
    return false;
  }
}
