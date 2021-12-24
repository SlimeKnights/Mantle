package slimeknights.mantle.inventory;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.hooks.BasicEventHooks;

public class CraftingCustomSlot extends ResultSlot {

  private final CraftingContainer craftMatrix;
  private final IContainerCraftingCustom callback;

  /**
   * @param callback          Container that gets the crafting call on crafting
   * @param player            Player that does the crafting
   * @param craftingInventory Inventory where the ingredients are taken from
   * @param craftResult       Inventory where the result is put
   */
  public CraftingCustomSlot(IContainerCraftingCustom callback, Player player, CraftingContainer craftingInventory, Container craftResult, int slotIndex, int xPosition, int yPosition) {
    super(player, craftingInventory, craftResult, slotIndex, xPosition, yPosition);

    this.craftMatrix = craftingInventory;
    this.callback = callback;
  }

  @Override
  public ItemStack onTake(Player playerIn, ItemStack stack) {
    BasicEventHooks.firePlayerCraftingEvent(playerIn, stack, this.craftMatrix);
    this.checkTakeAchievements(stack);

    this.callback.onCrafting(playerIn, stack, this.craftMatrix);

    return stack;
  }
}
