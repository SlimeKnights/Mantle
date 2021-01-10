package slimeknights.mantle.tileentity;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

public class MantleTileEntity extends TileEntity {

  public MantleTileEntity(TileEntityType<?> tileEntityTypeIn) {
    super(tileEntityTypeIn);
  }

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
  
  
  /* Syncing */

  /**
   * Write to NBT that is synced to the client in {@link #getUpdateTag()} and in {@link #write(CompoundNBT)}
   * @param nbt  NBT
   */
  protected void writeSynced(CompoundNBT nbt) {}

  @Override
  public CompoundNBT getUpdateTag() {
    CompoundNBT nbt = super.getUpdateTag();
    writeSynced(nbt);
    return nbt;
  }

  public CompoundNBT write(CompoundNBT nbt) {
    nbt = super.write(nbt);
    writeSynced(nbt);
    return nbt;
  }
}