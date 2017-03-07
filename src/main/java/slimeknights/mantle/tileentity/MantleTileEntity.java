package slimeknights.mantle.tileentity;

import net.minecraft.tileentity.TileEntity;

public class MantleTileEntity extends TileEntity {

  public boolean isClient() {
    return this.getWorld() != null && this.getWorld().isRemote;
  }
}
