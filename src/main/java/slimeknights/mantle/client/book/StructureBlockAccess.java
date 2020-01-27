package slimeknights.mantle.client.book;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.LightType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.gen.Heightmap.Type;
import net.minecraft.world.lighting.WorldLightManager;

import javax.annotation.Nullable;

public class StructureBlockAccess implements IWorldReader {

  private final StructureInfo data;
  private final BlockState[][][] structure;

  public StructureBlockAccess(StructureInfo data) {
    this.data = data;
    this.structure = data.data;
  }

  @Nullable
  @Override
  public TileEntity getTileEntity(BlockPos pos) {
    return null;
  }

  @Override
  public WorldLightManager getLightManager() {
    return null;
  }

  @Override
  public int getLightFor(LightType type, BlockPos pos) {
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
  public boolean isAirBlock(BlockPos pos) {
    return this.getBlockState(pos).getBlock() == Blocks.AIR;
  }

  @Override
  public Biome getBiome(BlockPos pos) {
    return null;
  }

  @Override
  public Biome getNoiseBiomeRaw(int p_225604_1_, int p_225604_2_, int p_225604_3_) {
    return null;
  }

  @Override
  public int getStrongPower(BlockPos pos, Direction direction) {
    return 0;
  }

  @Override
  public int getLightSubtracted(BlockPos pos, int amount) {
    return 0;
  }

  @Nullable
  @Override
  public IChunk getChunk(int x, int z, ChunkStatus requiredStatus, boolean nonnull) {
    return null;
  }

  @Override
  public boolean chunkExists(int chunkX, int chunkZ) {
    return false;
  }

  @Override
  public BlockPos getHeight(Type heightmapType, BlockPos pos) {
    return BlockPos.ZERO;
  }

  @Override
  public boolean canSeeSky(BlockPos pos) {
    return true;
  }

  @Override
  public int getHeight(Type heightmapType, int x, int z) {
    return 0;
  }

  @Override
  public int getSkylightSubtracted() {
    return 0;
  }

  @Override
  public BiomeManager getBiomeManager() {
    return null;
  }

  @Override
  public WorldBorder getWorldBorder() {
    return null;
  }

  @Override
  public boolean checkNoEntityCollision(Entity entityIn, VoxelShape shape) {
    return false;
  }

  @Override
  public boolean isRemote() {
    return false;
  }

  @Override
  public int getSeaLevel() {
    return 0;
  }

  @Override
  public Dimension getDimension() {
    return null;
  }

  @Override
  public IFluidState getFluidState(BlockPos pos) {
    return Fluids.EMPTY.getDefaultState();
  }
}
