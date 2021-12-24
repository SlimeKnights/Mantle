package slimeknights.mantle.inventory;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public interface IContainerCraftingCustom {

  void onCrafting(Player player, ItemStack output, Container craftMatrix);
}
