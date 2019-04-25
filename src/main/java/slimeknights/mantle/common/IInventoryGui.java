package slimeknights.mantle.common;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Used in conjunction with GuiHandler. Implement in a TE to have it open its GUI/Container.
 */
public interface IInventoryGui {

  Container createContainer(InventoryPlayer inventoryplayer, World world, BlockPos pos);

  @OnlyIn(Dist.CLIENT)
  GuiContainer createGui(InventoryPlayer inventoryplayer, World world, BlockPos pos);
}
