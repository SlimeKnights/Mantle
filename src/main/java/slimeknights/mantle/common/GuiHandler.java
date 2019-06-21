package slimeknights.mantle.common;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

/**
 * A GuiHandler implementation that forwards the container/gui creation to the TEs, eliminating the need for GUI IDs.
 */
public class GuiHandler implements IGuiHandler {

  @Override
  public Object getServerGuiElement(int ID, PlayerEntity player, World world, int x, int y, int z) {
    TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
    if(te instanceof IInventoryGui) {
      return ((IInventoryGui) te).createContainer(player.inventory, world, new BlockPos(x, y, z));
    }
    return null;
  }

  @Override
  public Object getClientGuiElement(int ID, PlayerEntity player, World world, int x, int y, int z) {
    TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
    if(te instanceof IInventoryGui) {
      return ((IInventoryGui) te).createGui(player.inventory, world, new BlockPos(x, y, z));
    }
    return null;
  }
}
