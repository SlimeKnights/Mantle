package slimeknights.mantle.inventory;

import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

/** Fixes issues with the item handler slot in NeoForge */
public class ItemHandlerCopySlot extends net.neoforged.neoforge.items.ItemHandlerCopySlot {
	public ItemHandlerCopySlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
		super(itemHandler, index, xPosition, yPosition);
	}

  public ItemHandlerCopySlot(SlotItemHandler slot) {
    super(slot);
  }

  /** Fixes calls to {@link #allowModification(Player)} failing when the slot is empty. Used by JEI to determine whether a slot is valid for recipe transfer. */
  @Override
  public boolean mayPickup(Player playerIn) {
    return getItem().isEmpty() || super.mayPickup(playerIn);
  }
}
