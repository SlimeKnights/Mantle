package slimeknights.mantle.tileentity;

import net.minecraft.tileentity.TileEntity;

public class MantleTileEntity extends TileEntity {

  public boolean isClient() {
    return this.getWorld() != null && this.getWorld().isRemote;
  }

  /**
   * Marks the chunk dirty without performing comparator updates or block state checks
   * Used since most of our markDirty calls only adjust TE data
   */
  public void markDirtyFast() {
    if (world != null) {
      world.markChunkDirty(pos, this);
    }
  }
}
