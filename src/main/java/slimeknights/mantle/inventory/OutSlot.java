package slimeknights.mantle.inventory;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

/** Slot that can only be taken out of */
public class OutSlot extends Slot {

  public OutSlot(Inventory inventoryIn, int index, int xPosition, int yPosition) {
    super(inventoryIn, index, xPosition, yPosition);
  }

  @Override
  public boolean canInsert(ItemStack stack) {
    return false;
  }
}
