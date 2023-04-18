package slimeknights.mantle.inventory;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

/** Forge still uses dumb vanilla logic for determining slot limits instead of their own method */
public class SmartItemHandlerSlot extends SlotItemHandler {
	public SmartItemHandlerSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
		super(itemHandler, index, xPosition, yPosition);
	}

	@Override
	public int getMaxStackSize(ItemStack stack) {
		return Math.min(stack.getMaxStackSize(), getItemHandler().getSlotLimit(getSlotIndex()));
	}
}
