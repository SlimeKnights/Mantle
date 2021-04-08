package slimeknights.mantle.client.book;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.LightType;
import net.minecraft.world.WorldView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class StructureBlockAccess implements WorldView {

  private final StructureInfo data;
  private final BlockState[][][] structure;

  public StructureBlockAccess(StructureInfo data) {
    this.data = data;
    this.structure = data.data;
  }

  @Nullable
  @Override
  public BlockEntity getBlockEntity(BlockPos pos) {
    return null;
  }

  @Override
  public float getBrightness(Direction p_230487_1_, boolean p_230487_2_) {
    return 0;
  }

  @Override
  public LightingProvider getLightingProvider() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getLightLevel(LightType type, BlockPos pos) {
    return 15 << 20 | 15 << 4;
  }

  @Override
  public BlockState getBlockState(BlockPos pos) {
    int x = pos.getX();
    int y = pos.getY();
    int z = pos.getZ();

    if (y >= 0 && y < this.structure.length) {
      if (x >= 0 && x < this.structure[y].length) {
        if (z >= 0 && z < this.structure[y][x].length) {
          int index = y * (this.data.structureLength * this.data.structureWidth) + x * this.data.structureWidth + z;
          if (index <= this.data.getLimiter()) {
            return this.structure[y][x][z] != null ? this.structure[y][x][z] : Blocks.AIR.getDefaultState();
          }
        }
      }
    }
    return Blocks.AIR.getDefaultState();
  }

  @Override
  public boolean isAir(BlockPos pos) {
    return this.getBlockState(pos).getBlock() == Blocks.AIR;
  }

  @Override
  public Biome getBiome(BlockPos pos) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Biome getGeneratorStoredBiome(int x, int y, int z) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getStrongRedstonePower(BlockPos pos, Direction direction) {
    return 0;
  }

  @Override
  public int getBaseLightLevel(BlockPos pos, int amount) {
    return 0;
  }

  @Nullable
  @Override
  public Chunk getChunk(int x, int z, ChunkStatus requiredStatus, boolean nonnull) {
    return null;
  }

  @Deprecated
  @Override
  public boolean isChunkLoaded(int chunkX, int chunkZ) {
    return false;
  }

  @Override
  public BlockPos getTopPosition(Type heightmapType, BlockPos pos) {
    return BlockPos.ORIGIN;
  }

  @Override
  public boolean isSkyVisible(BlockPos pos) {
    return true;
  }

  @Override
  public int getTopY(Type heightmapType, int x, int z) {
    return 0;
  }

  @Override
  public int getAmbientDarkness() {
    return 0;
  }

  @Override
  public BiomeAccess getBiomeAccess() {
    throw new UnsupportedOperationException();
  }

  @Override
  public WorldBorder getWorldBorder() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean intersectsEntities(@Nullable Entity entityIn, VoxelShape shape) {
    return false;
  }

  @Override
  public Stream<VoxelShape> getEntityCollisions(@Nullable Entity p_230318_1_, Box p_230318_2_, Predicate<Entity> p_230318_3_) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isClient() {
    return false;
  }

  @Deprecated
  @Override
  public int getSeaLevel() {
    return 0;
  }

  @Override
  public DimensionType getDimension() {
    throw new UnsupportedOperationException();
  }

  @Override
  public FluidState getFluidState(BlockPos pos) {
    return Fluids.EMPTY.getDefaultState();
  }
}
