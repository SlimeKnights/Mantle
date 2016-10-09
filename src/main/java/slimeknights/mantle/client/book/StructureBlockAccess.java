package slimeknights.mantle.client.book;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nullable;

public class StructureBlockAccess implements IBlockAccess {

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
  public WorldType getWorldType() {
    return null;
  }

  @Override
  public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
    return false;
  }
}
