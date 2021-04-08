package slimeknights.mantle.tileentity;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.CompoundTag;

public class MantleTileEntity extends BlockEntity {

  public MantleTileEntity(BlockEntityType<?> tileEntityTypeIn) {
    super(tileEntityTypeIn);
  }

  public boolean isClient() {
    return this.getWorld() != null && this.getWorld().isClient;
  }

  /**
   * Marks the chunk dirty without performing comparator updates or block state checks
   * Used since most of our markDirty calls only adjust TE data
   */
  public void markDirtyFast() {
    if (world != null) {
      world.markDirty(pos, this);
    }
  }
  
  
  /* Syncing */

  /**
   * Write to NBT that is synced to the client in {@link #toInitialChunkDataTag()} and in {@link #toTag(CompoundTag)}
   * @param nbt  NBT
   */
  protected void writeSynced(CompoundTag nbt) {}

  @Override
  public CompoundTag toInitialChunkDataTag() {
    CompoundTag nbt = super.toInitialChunkDataTag();
    writeSynced(nbt);
    return nbt;
  }

  public CompoundTag toTag(CompoundTag nbt) {
    nbt = super.toTag(nbt);
    writeSynced(nbt);
    return nbt;
  }
}