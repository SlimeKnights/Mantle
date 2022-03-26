// Credit to Immersive Engineering and blusunrize for this class
// See: https://github.com/BluSunrize/ImmersiveEngineering/blob/1.18/src/main/java/blusunrize/immersiveengineering/common/util/fakeworld/TemplateWorld.java
package slimeknights.mantle.client.book.structure.level;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.ticks.BlackholeTickAccess;
import net.minecraft.world.ticks.LevelTickAccess;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * World implementation for the book structures
 */
public class TemplateLevel extends Level {

  private final Map<String, MapItemSavedData> maps = new HashMap<>();
  private final Scoreboard scoreboard = new Scoreboard();
  private final RecipeManager recipeManager = new RecipeManager();
  private final TemplateChunkSource chunkSource;
  private final RegistryAccess registries = Objects.requireNonNull(Minecraft.getInstance().level).registryAccess();

  public TemplateLevel(List<StructureBlockInfo> blocks, Predicate<BlockPos> shouldShow) {
    super(
      new FakeLevelData(), Level.OVERWORLD, Objects.requireNonNull(Minecraft.getInstance().level).registryAccess().registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY).getHolderOrThrow(DimensionType.OVERWORLD_LOCATION),
      () -> InactiveProfiler.INSTANCE, true, false, 0
    );

    this.chunkSource = new TemplateChunkSource(blocks, this, shouldShow);
  }

  @Override
  public void sendBlockUpdated(@Nonnull BlockPos pos, @Nonnull BlockState oldState, @Nonnull BlockState newState, int flags) {
  }

  @Override
  public void playSound(@Nullable Player player, double x, double y, double z, @Nonnull SoundEvent soundIn, @Nonnull SoundSource category, float volume, float pitch) {
  }

  @Override
  public void playSound(@Nullable Player playerIn, @Nonnull Entity entityIn, @Nonnull SoundEvent eventIn, @Nonnull SoundSource categoryIn, float volume, float pitch) {
  }

  @Override
  public String gatherChunkSourceStats() {
    return chunkSource.gatherStats();
  }

  @Nullable
  @Override
  public Entity getEntity(int id) {
    return null;
  }

  @Nullable
  @Override
  public MapItemSavedData getMapData(@Nonnull String mapName) {
    return this.maps.get(mapName);
  }

  @Override
  public void setMapData(String mapId, MapItemSavedData mapDataIn) {
    this.maps.put(mapId, mapDataIn);
  }

  @Override
  public int getFreeMapId() {
    return this.maps.size();
  }

  @Override
  public void destroyBlockProgress(int breakerId, @Nonnull BlockPos pos, int progress) {
  }

  @Nonnull
  @Override
  public Scoreboard getScoreboard() {
    return this.scoreboard;
  }

  @Nonnull
  @Override
  public RecipeManager getRecipeManager() {
    return this.recipeManager;
  }

  @Override
  protected LevelEntityGetter<Entity> getEntities() {
    return FakeEntityGetter.INSTANCE;
  }

  @Nonnull
  @Override
  public LevelTickAccess<Block> getBlockTicks() {
    return BlackholeTickAccess.emptyLevelList();
  }

  @Nonnull
  @Override
  public LevelTickAccess<Fluid> getFluidTicks() {
    return BlackholeTickAccess.emptyLevelList();
  }

  @Nonnull
  @Override
  public ChunkSource getChunkSource() {
    return this.chunkSource;
  }

  @Override
  public void levelEvent(@Nullable Player player, int type, @Nonnull BlockPos pos, int data) {
  }

  @Override
  public void gameEvent(@Nullable Entity pEntity, GameEvent pEvent, BlockPos pPos) {
  }

  @Nonnull
  @Override
  public RegistryAccess registryAccess() {
    return this.registries;
  }

  @Override
  public float getShade(@Nonnull Direction p_230487_1_, boolean p_230487_2_) {
    return 1;
  }

  @Nonnull
  @Override
  public List<? extends Player> players() {
    return ImmutableList.of();
  }

  @Nonnull
  @Override
  public Holder<Biome> getUncachedNoiseBiome(int x, int y, int z) {
    return registries.registryOrThrow(Registry.BIOME_REGISTRY).getHolderOrThrow(Biomes.PLAINS);
  }
}
