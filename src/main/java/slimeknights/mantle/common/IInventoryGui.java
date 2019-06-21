package slimeknights.mantle.common;

import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Used in conjunction with GuiHandler. Implement in a TE to have it open its GUI/Container.
 */
public interface IInventoryGui {

  Container createContainer(PlayerInventory inventoryplayer, World world, BlockPos pos);

  @OnlyIn(Dist.CLIENT)
  ContainerScreen createGui(PlayerInventory inventoryplayer, World world, BlockPos pos);
}
