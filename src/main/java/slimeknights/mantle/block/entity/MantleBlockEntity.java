package slimeknights.mantle.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/** Block entity with additional utilities to make NBT syncing easier. */
public class MantleBlockEntity extends BlockEntity {

  public MantleBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
    super(type, pos, state);
  }

  /** Checks if the level is not null and its serverside side */
  public boolean isServerSide() {
    return this.level != null && !level.isClientSide;
  }

  /** Checks if the level is not null and its client side */
  public boolean isClient() {
    return this.level != null && level.isClientSide;
  }

  /**
   * Marks the chunk dirty without performing comparator updates (twice!!) or block state checks
   * Used since most of our markDirty calls only adjust TE data
   * @see #setChanged()
   */
  public void setChangedFast() {
    if (level != null) {
      level.blockEntityChanged(worldPosition);
    }
  }
  
  
  /* Syncing */

  /**
   * If true, this TE syncs when {@link net.minecraft.world.level.Level#blockUpdated(BlockPos, Block) is called
   * Syncs data from {@link #saveSynced(CompoundTag, Provider) }
   */
  protected boolean shouldSyncOnUpdate() {
    return false;
  }

  @Override
  @Nullable
  public ClientboundBlockEntityDataPacket getUpdatePacket() {
    return shouldSyncOnUpdate() ? ClientboundBlockEntityDataPacket.create(this) : null;
  }

  /**
   * Write to NBT that is synced to the client in {@link #getUpdateTag(Provider)} and in {@link #saveAdditional(CompoundTag, Provider)}
   *
   * @param nbt         NBT
   * @param registries  Registry access for saving
   */
  protected void saveSynced(CompoundTag nbt, Provider registries) {}

  @Override
  public CompoundTag getUpdateTag(Provider registries) {
    CompoundTag nbt = new CompoundTag();
    saveSynced(nbt, registries);
    return nbt;
  }

  @Override
  public void saveAdditional(CompoundTag nbt, Provider registries) {
    super.saveAdditional(nbt, registries);
    saveSynced(nbt, registries);
  }
}
