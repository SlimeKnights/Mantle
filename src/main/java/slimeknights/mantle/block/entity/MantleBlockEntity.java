package slimeknights.mantle.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class MantleBlockEntity extends BlockEntity {
  protected static final HolderLookup.Provider BUILTIN_LOOKUP = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);

  public MantleBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
    super(type, pos, state);
  }

  public boolean isClient() {
    return this.getLevel() != null && this.getLevel().isClientSide;
  }

  /**
   * Marks the chunk dirty without performing comparator updates (twice!!) or block state checks
   * Used since most of our markDirty calls only adjust TE data
   */
  @SuppressWarnings("deprecation")
  public void setChangedFast() {
    if (level != null) {
      if (level.hasChunkAt(worldPosition)) {
        level.getChunkAt(worldPosition).setUnsaved(true);
      }
    }
  }

  /* Syncing */

  /**
   * If true, this TE syncs when {@link net.minecraft.world.level.Level#blockUpdated(BlockPos, Block) is called
   * Syncs data from {@link #saveSynced(CompoundTag, HolderLookup.Provider)}
   */
  protected boolean shouldSyncOnUpdate() {
    return false;
  }

  @Override
  @Nullable
  public ClientboundBlockEntityDataPacket getUpdatePacket() {
    // number is just used for vanilla, -1 ensures it skips all instanceof checks as its not a vanilla TE
    return shouldSyncOnUpdate() ? ClientboundBlockEntityDataPacket.create(this) : null;
  }

  /**
   * Write to NBT that is synced to the client in {@link #getUpdateTag(HolderLookup.Provider)} and in {@link #saveAdditional(CompoundTag, HolderLookup.Provider)}
   * @param nbt         NBT
   * @param registries  Registry lookup
   */
  protected void saveSynced(CompoundTag nbt, HolderLookup.Provider registries) {}

  /** Compatibility overload for code still using the old no-registry NBT hook. */
  @Deprecated(forRemoval = true)
  protected void saveSynced(CompoundTag nbt) {
    saveSynced(nbt, BUILTIN_LOOKUP);
  }

  @Override
  public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
    CompoundTag nbt = new CompoundTag();
    saveSynced(nbt, registries);
    return nbt;
  }

  /** Compatibility overload for code still using the old no-registry NBT hook. */
  @Deprecated(forRemoval = true)
  public CompoundTag getUpdateTag() {
    return getUpdateTag(BUILTIN_LOOKUP);
  }

  /** Compatibility overload for code still using the old block entity load hook. */
  @Deprecated(forRemoval = true)
  public void load(CompoundTag tags) {
    loadAdditional(tags, BUILTIN_LOOKUP);
  }

  /** Compatibility overload for code still using the old no-registry NBT save hook. */
  @Deprecated(forRemoval = true)
  protected void saveAdditional(CompoundTag nbt) {
    saveAdditional(nbt, BUILTIN_LOOKUP);
  }

  /** Compatibility overload for code still using the old no-registry update tag hook. */
  @Deprecated(forRemoval = true)
  public void handleUpdateTag(CompoundTag tag) {
    handleUpdateTag(tag, BUILTIN_LOOKUP);
  }

  @Override
  public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
    super.saveAdditional(nbt, registries);
    saveSynced(nbt, registries);
  }
}
