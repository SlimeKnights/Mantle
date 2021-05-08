package slimeknights.mantle.tileentity;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants.NBT;

import javax.annotation.Nullable;

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
   * If true, this TE syncs when {@link net.minecraft.world.World#notifyBlockUpdate(BlockPos, BlockState, BlockState, int)} is called
   * Syncs data from {@link #writeSynced(CompoundNBT)}
   */
  protected boolean shouldSyncOnUpdate() {
    return false;
  }

  @Override
  @Nullable
  public SUpdateTileEntityPacket getUpdatePacket() {
    // number is just used for vanilla, -1 ensures it skips all instanceof checks as its not a vanilla TE
    return shouldSyncOnUpdate() ? new SUpdateTileEntityPacket(this.pos, -1, this.getUpdateTag()) : null;
  }

  @Override
  public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
    this.read(this.getBlockState(), pkt.getNbtCompound());
  }

  /**
   * Write to NBT that is synced to the client in {@link #getUpdateTag()} and in {@link #write(CompoundNBT)}
   * @param nbt  NBT
   */
  protected void writeSynced(CompoundNBT nbt) {}

  @Override
  public CompoundNBT getUpdateTag() {
    CompoundNBT nbt = super.getUpdateTag();
    // forge just directly puts the data into the update tag, which on dedicated server can lead to client and server both writing to the same tag object
    // fix that by copying forge data before syncing it
    if (nbt.contains("ForgeData", NBT.TAG_COMPOUND)) {
      CompoundNBT forgeData = nbt.getCompound("ForgeData");
      if (forgeData == this.getTileData()) {
        nbt.put("ForgeData", forgeData.copy());
      }
    }
    writeSynced(nbt);
    return nbt;
  }

  @Override
  public CompoundNBT write(CompoundNBT nbt) {
    nbt = super.write(nbt);
    writeSynced(nbt);
    return nbt;
  }
}
