// Credit to Immersive Engineering and blusunrize for this class
// See: https://github.com/BluSunrize/ImmersiveEngineering/blob/1.16.5/src/main/java/blusunrize/immersiveengineering/common/util/fakeworld/TemplateWorld.java
package slimeknights.mantle.client.book.structure.world;

import com.google.common.collect.ImmutableList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.tags.TagContainer;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.EmptyTickList;
import net.minecraft.world.level.TickList;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class TemplateWorld extends Level {

  private final Map<String, MapItemSavedData> maps = new HashMap<>();
  private final Scoreboard scoreboard = new Scoreboard();
  private final RecipeManager recipeManager = new RecipeManager();
  private final TemplateChunkProvider chunkProvider;
  private final RegistryAccess registries = RegistryAccess.builtin();

  public TemplateWorld(List<StructureBlockInfo> blocks, Predicate<BlockPos> shouldShow) {
    super(
      new FakeSpawnInfo(), Level.OVERWORLD, DimensionType.DEFAULT_OVERWORLD,
      () -> InactiveProfiler.INSTANCE, true, false, 0
    );

    this.chunkProvider = new TemplateChunkProvider(blocks, this, shouldShow);
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
  public void setMapData(@Nonnull MapItemSavedData mapDataIn) {
    this.maps.put(mapDataIn.getId(), mapDataIn);
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

  @Nonnull
  @Override
  public TagContainer getTagManager() {
    return TagContainer.EMPTY;
  }

  @Nonnull
  @Override
  public TickList<Block> getBlockTicks() {
    return EmptyTickList.empty();
  }

  @Nonnull
  @Override
  public TickList<Fluid> getLiquidTicks() {
    return EmptyTickList.empty();
  }

  @Nonnull
  @Override
  public ChunkSource getChunkSource() {
    return this.chunkProvider;
  }

  @Override
  public void levelEvent(@Nullable Player player, int type, @Nonnull BlockPos pos, int data) {
  }

  @Nonnull
  @Override
  public RegistryAccess registryAccess() {
    return this.registries;
  }

  @Override
  public float getShade(@Nonnull Direction p_230487_1_, boolean p_230487_2_) {
    return 0;
  }

  @Nonnull
  @Override
  public List<? extends Player> players() {
    return ImmutableList.of();
  }

  @Nonnull
  @Override
  public Biome getUncachedNoiseBiome(int x, int y, int z) {
    return this.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).getOrThrow(Biomes.PLAINS);
  }
}
