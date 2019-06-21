package slimeknights.mantle.inventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public interface IContainerCraftingCustom {

  void onCrafting(PlayerEntity player, ItemStack output, IInventory craftMatrix);
}
