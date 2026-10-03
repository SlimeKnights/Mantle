// Credit to Immersive Engineering and blusunrize for this class
// See: https://github.com/BluSunrize/ImmersiveEngineering/blob/1.18/src/main/java/blusunrize/immersiveengineering/common/util/fakeworld/TemplateWorld.java
package slimeknights.mantle.client.book.structure.level;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;
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
@SuppressWarnings("UnstableApiUsage")
public class TemplateLevel extends Level {
  private final Map<MapId, MapItemSavedData> maps = new HashMap<>();
  @Getter
  private final Scoreboard scoreboard = new Scoreboard();
  @Getter
  private final RecipeManager recipeManager;
  @Getter @Accessors(fluent = true)
  private final TickRateManager tickRateManager = new TickRateManager();
  @Getter
  private final TemplateChunkSource chunkSource;
  @Getter @Accessors(fluent = true)
  private final PotionBrewing potionBrewing;

  public TemplateLevel(List<StructureBlockInfo> blocks, Predicate<BlockPos> shouldShow) {
    super(
      FakeLevelData.INSTANCE, Level.OVERWORLD, Objects.requireNonNull(Minecraft.getInstance().level).registryAccess(),
      Objects.requireNonNull(Minecraft.getInstance().level).registryAccess().registryOrThrow(Registries.DIMENSION_TYPE).getHolderOrThrow(BuiltinDimensionTypes.OVERWORLD),
      () -> InactiveProfiler.INSTANCE, true, false, 0, 0
    );
    this.recipeManager = new RecipeManager(this.registryAccess());
    this.potionBrewing = new PotionBrewing.Builder(FeatureFlagSet.of()).build();
    this.chunkSource = new TemplateChunkSource(blocks, this, shouldShow);
  }

  @Override
  public FeatureFlagSet enabledFeatures() {
    return FeatureFlagSet.of();
  }


  /* Blocks */

  @Override
  public String gatherChunkSourceStats() {
    return chunkSource.gatherStats();
  }

  @Override
  public Holder<Biome> getUncachedNoiseBiome(int x, int y, int z) {
    return registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(Biomes.PLAINS);
  }

  @Override
  public void sendBlockUpdated(BlockPos pos, BlockState oldState, BlockState newState, int flags) {}

  @Override
  public void destroyBlockProgress(int breakerId, BlockPos pos, int progress) {}

  @Override
  public float getShade(Direction direction, boolean shade) {
    return 1;
  }


  /* Time */

  @Override
  public float getDayTimeFraction() {
    return 0;
  }

  @Override
  public void setDayTimeFraction(float v) {}

  @Override
  public float getDayTimePerTick() {
    return 0;
  }

  @Override
  public void setDayTimePerTick(float v) {}


  /* Ticks */

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


  /* Entities */

  @Nullable
  @Override
  public Entity getEntity(int id) {
    return null;
  }

  @Override
  public List<? extends Player> players() {
    return List.of();
  }

  @Override
  protected LevelEntityGetter<Entity> getEntities() {
    return FakeEntityGetter.INSTANCE;
  }


  /* Maps. TODO: do we need this? */

  @Nullable
  @Override
  public MapItemSavedData getMapData(MapId mapId) {
    return this.maps.get(mapId);
  }

  @Override
  public void setMapData(MapId mapId, MapItemSavedData mapDataIn) {
    this.maps.put(mapId, mapDataIn);
  }

  @Override
  public MapId getFreeMapId() {
    return new MapId(this.maps.size());
  }


  /* Sounds */

  @Override
  public void playSeededSound(@Nullable Player pPlayer, double pX, double pY, double pZ, Holder<SoundEvent> pSound, SoundSource pSource, float pVolume, float pPitch, long pSeed) {}

  @Override
  public void playSeededSound(@Nullable Player pPlayer, Entity pEntity, Holder<SoundEvent> pSound, SoundSource pCategory, float pVolume, float pPitch, long pSeed) {}


  /* Events */

  @Override
  public void levelEvent(@Nullable Player player, int type, BlockPos pos, int data) {}

  @Override
  public void gameEvent(Holder<GameEvent> event, Vec3 position, Context context) {}
}
