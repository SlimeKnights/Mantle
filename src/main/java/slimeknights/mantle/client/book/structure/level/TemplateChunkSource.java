// Credit to Immersive Engineering and blusunrize for this class
// See: https://github.com/BluSunrize/ImmersiveEngineering/blob/1.18/src/main/java/blusunrize/immersiveengineering/common/util/fakeworld/TemplateChunkProvider.java
package slimeknights.mantle.client.book.structure.level;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.level.lighting.LevelLightEngine;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TemplateChunkSource extends ChunkSource {

  private final Map<ChunkPos, ChunkAccess> chunks;
  private final Level level;
  private final LevelLightEngine lightManager;

  public TemplateChunkSource(List<StructureBlockInfo> blocks, Level level, Predicate<BlockPos> shouldShow) {
    this.level = level;
    this.lightManager = new LevelLightEngine(this, true, true);
    Map<ChunkPos, List<StructureBlockInfo>> byChunk = new HashMap<>();

    for (StructureBlockInfo info : blocks) {
      byChunk.computeIfAbsent(new ChunkPos(info.pos), $ -> new ArrayList<>()).add(info);
    }

    this.chunks = byChunk.entrySet().stream()
      .map(e -> Pair.of(e.getKey(), new TemplateChunk(level, e.getKey(), e.getValue(), shouldShow)))
      .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
  }

  @Nullable
  @Override
  public ChunkAccess getChunk(int chunkX, int chunkZ, @Nonnull ChunkStatus requiredStatus, boolean load) {
    return this.chunks.computeIfAbsent(new ChunkPos(chunkX, chunkZ), p -> new EmptyLevelChunk(level, p, BuiltinRegistries.BIOME.getHolderOrThrow(Biomes.PLAINS)));
  }

  @Override
  public void tick(BooleanSupplier hasMoreTime, boolean tickChunks) {}

  @Override
  public String gatherStats() {
    return "?";
  }

  @Override
  public int getLoadedChunksCount() {
    return chunks.size();
  }

  @Nonnull
  @Override
  public LevelLightEngine getLightEngine() {
    return this.lightManager;
  }

  @Override
  public BlockGetter getLevel() {
    return this.level;
  }
}
