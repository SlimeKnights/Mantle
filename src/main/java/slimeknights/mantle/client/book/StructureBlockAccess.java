package slimeknights.mantle.client.book;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.fluid.IFluidState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Fluids;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.EnumLightType;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.gen.Heightmap.Type;

import java.util.function.Predicate;

import javax.annotation.Nullable;

public class StructureBlockAccess implements IWorldReader
{
  private final StructureInfo data;
  private final IBlockState[][][] structure;

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
  public int getCombinedLight(BlockPos pos, int lightValue) {
    // full brightness always
    return 15 << 20 | 15 << 4;
  }

  @Override
  public int getLightFor(EnumLightType type, BlockPos pos) {
    return 15 << 20 | 15 << 4;
  }

  @Override
  public IBlockState getBlockState(BlockPos pos) {
    int x = pos.getX();
    int y = pos.getY();
    int z = pos.getZ();

    if(y >= 0 && y < structure.length) {
      if(x >= 0 && x < structure[y].length) {
        if(z >= 0 && z < structure[y][x].length) {
          int index = y * (data.structureLength * data.structureWidth) + x * data.structureWidth + z;
          if(index <= data.getLimiter()) {
            return structure[y][x][z] != null ? structure[y][x][z] : Blocks.AIR.getDefaultState();
          }
        }
      }
    }
    return Blocks.AIR.getDefaultState();
  }

  @Override
  public boolean isAirBlock(BlockPos pos) {
    return getBlockState(pos).getBlock() == Blocks.AIR;
  }

  @Override
  public Biome getBiome(BlockPos pos) {
    return null;
  }

  @Override
  public int getStrongPower(BlockPos pos, EnumFacing direction) {
    return 0;
  }

  @Override
  public int getLightSubtracted(BlockPos pos, int amount) {
    return 0;
  }

  @Override
  public boolean isChunkLoaded(int x, int z, boolean allowEmpty) {
    return false;
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
  public EntityPlayer getClosestPlayer(double x, double y, double z, double distance, Predicate<Entity> predicate) {
    return null;
  }

  @Override
  public int getSkylightSubtracted() {
    return 0;
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
