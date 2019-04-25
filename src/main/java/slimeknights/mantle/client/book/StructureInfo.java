package slimeknights.mantle.client.book;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

import java.util.Map;
import java.util.Optional;

import net.minecraft.state.IProperty;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import slimeknights.mantle.client.book.data.element.BlockData;

public class StructureInfo {
  public IBlockState[][][] data;
  public int blockCount = 0;
  public int[] countPerLevel;
  public int structureHeight = 0;
  public int structureLength = 0;
  public int structureWidth = 0;
  public int showLayer = -1;

  private int blockIndex = 0;
  private int maxBlockIndex;

  public StructureInfo(int length, int height, int width, BlockData[] blockData) {
    this.structureWidth = width;
    this.structureHeight = height;
    this.structureLength = length;
    IBlockState[][][] states = new IBlockState[height][length][width];

    for(int y = 0; y < height; y++) {
      for(int x = 0; x < length; x++) {
        for(int z = 0; z < width; z++) {
          for(BlockData data : blockData) {
            if(inside(x, y, z, data.pos, data.endPos)) {
              states[y][x][z] = convert(data);
              break;
            }
          }
        }
      }
    }

    data = states;
    maxBlockIndex = blockIndex = structureHeight * structureLength * structureWidth;
  }

  private IBlockState convert(BlockData data) {
    Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(data.block));
    if(block == null) {
      return Blocks.AIR.getDefaultState();
    }
    IBlockState state;
    if(data.state == null || data.state.isEmpty()) {
      state = block.getStateFromMeta(data.meta);
    }
    else {
      state = block.getDefaultState();

      for(Map.Entry<String, String> entry : data.state.entrySet()) {
        Optional<IProperty<?>> property = state.getProperties().stream().filter(iProperty -> entry.getKey().equals(iProperty.getName())).findFirst();

        if(property.isPresent()) {
          state = setProperty(state, property.get(), entry.getValue());
        }
      }
    }
    return state;
  }

  private <T extends Comparable<T>> IBlockState setProperty(IBlockState state, IProperty<T> prop, String valueString) {
    java.util.Optional<T> value = prop.parseValue(valueString);
    if(value.isPresent()) {
      state = state.with(prop, value.get());
    }
    return state;
  }


  private boolean inside(int x, int y, int z, int[] rangeStart, int[] rangeEnd) {
    if(x >= rangeStart[0] && x <= rangeEnd[0]) {
      if(y >= rangeStart[1] && y <= rangeEnd[1]) {
        if(z >= rangeStart[2] && z <= rangeEnd[2]) {
          return true;
        }
      }
    }

    return false;
  }

  public void setShowLayer(int layer) {
    showLayer = layer;
    blockIndex = (layer + 1) * (structureLength * structureWidth) - 1;
  }

  public void reset() {
    blockIndex = maxBlockIndex;
  }

  public boolean canStep() {
    int index = blockIndex;
    do {
      if(++index >= maxBlockIndex) {
        return false;
      }
    } while(isEmpty(index));
    return true;
  }

  public void step() {
    int start = blockIndex;
    do {
      if(++blockIndex >= maxBlockIndex) {
        blockIndex = 0;
      }
    } while(isEmpty(blockIndex) && blockIndex != start);
  }

  private boolean isEmpty(int index) {
    int y = index / (structureLength * structureWidth);
    int r = index % (structureLength * structureWidth);
    int x = r / structureWidth;
    int z = r % structureWidth;

    return data[y][x][z] == null || data[y][x][z].getBlock() == Blocks.AIR;
  }

  public int getLimiter() {
    return blockIndex;
  }
}
