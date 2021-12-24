package slimeknights.mantle.tileentity;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants.NBT;

import javax.annotation.Nullable;

public class MantleTileEntity extends BlockEntity {

  public MantleTileEntity(BlockEntityType<?> tileEntityTypeIn) {
    super(tileEntityTypeIn);
  }

  public boolean isClient() {
    return this.getLevel() != null && this.getLevel().isClientSide;
  }

  /**
   * Marks the chunk dirty without performing comparator updates or block state checks
   * Used since most of our markDirty calls only adjust TE data
   */
  public void markDirtyFast() {
    if (level != null) {
      level.blockEntityChanged(worldPosition, this);
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
  public ClientboundBlockEntityDataPacket getUpdatePacket() {
    // number is just used for vanilla, -1 ensures it skips all instanceof checks as its not a vanilla TE
    return shouldSyncOnUpdate() ? new ClientboundBlockEntityDataPacket(this.worldPosition, -1, this.getUpdateTag()) : null;
  }

  @Override
  public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
    this.load(this.getBlockState(), pkt.getTag());
  }

  /**
   * Write to NBT that is synced to the client in {@link #getUpdateTag()} and in {@link #write(CompoundNBT)}
   * @param nbt  NBT
   */
  protected void writeSynced(CompoundTag nbt) {}

  @Override
  public CompoundTag getUpdateTag() {
    CompoundTag nbt = super.getUpdateTag();
    // forge just directly puts the data into the update tag, which on dedicated server can lead to client and server both writing to the same tag object
    // fix that by copying forge data before syncing it
    if (nbt.contains("ForgeData", NBT.TAG_COMPOUND)) {
      CompoundTag forgeData = nbt.getCompound("ForgeData");
      if (forgeData == this.getTileData()) {
        nbt.put("ForgeData", forgeData.copy());
      }
    }
    writeSynced(nbt);
    return nbt;
  }

  @Override
  public CompoundTag save(CompoundTag nbt) {
    nbt = super.save(nbt);
    writeSynced(nbt);
    return nbt;
  }
}
