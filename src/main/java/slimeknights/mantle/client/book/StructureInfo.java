package slimeknights.mantle.client.book;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.Map;
import java.util.Optional;
import slimeknights.mantle.client.book.data.element.BlockData;

public class StructureInfo {

  public BlockState[][][] data;
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
    BlockState[][][] states = new BlockState[height][length][width];

    for (int y = 0; y < height; y++) {
      for (int x = 0; x < length; x++) {
        for (int z = 0; z < width; z++) {
          for (BlockData data : blockData) {
            if (this.inside(x, y, z, data.pos, data.endPos)) {
              states[y][x][z] = this.convert(data);
              break;
            }
          }
        }
      }
    }

    this.data = states;
    this.maxBlockIndex = this.blockIndex = this.structureHeight * this.structureLength * this.structureWidth;
  }

  private BlockState convert(BlockData data) {
    Block block = Registry.BLOCK.get(new Identifier(data.block));
    if (block == null) {
      return Blocks.AIR.getDefaultState();
    }
    BlockState state = block.getDefaultState();

    if (data.state != null && !data.state.isEmpty()) {
      for (Map.Entry<String, String> entry : data.state.entrySet()) {
        Optional<Property<?>> property = state.getProperties().stream().filter(iProperty -> entry.getKey().equals(iProperty.getName())).findFirst();

        if (property.isPresent()) {
          state = this.setProperty(state, property.get(), entry.getValue());
        }
      }
    }

    return state;
  }

  private <T extends Comparable<T>> BlockState setProperty(BlockState state, Property<T> prop, String valueString) {
    java.util.Optional<T> value = prop.parse(valueString);
    if (value.isPresent()) {
      state = state.with(prop, value.get());
    }
    return state;
  }

  private boolean inside(int x, int y, int z, int[] rangeStart, int[] rangeEnd) {
    if (x >= rangeStart[0] && x <= rangeEnd[0]) {
      if (y >= rangeStart[1] && y <= rangeEnd[1]) {
        return z >= rangeStart[2] && z <= rangeEnd[2];
      }
    }

    return false;
  }

  public void setShowLayer(int layer) {
    this.showLayer = layer;
    this.blockIndex = (layer + 1) * (this.structureLength * this.structureWidth) - 1;
  }

  public void reset() {
    this.blockIndex = this.maxBlockIndex;
  }

  public boolean canStep() {
    int index = this.blockIndex;
    do {
      if (++index >= this.maxBlockIndex) {
        return false;
      }
    }
    while (this.isEmpty(index));
    return true;
  }

  public void step() {
    int start = this.blockIndex;
    do {
      if (++this.blockIndex >= this.maxBlockIndex) {
        this.blockIndex = 0;
      }
    }
    while (this.isEmpty(this.blockIndex) && this.blockIndex != start);
  }

  private boolean isEmpty(int index) {
    int y = index / (this.structureLength * this.structureWidth);
    int r = index % (this.structureLength * this.structureWidth);
    int x = r / this.structureWidth;
    int z = r % this.structureWidth;

    return this.data[y][x][z] == null || this.data[y][x][z].getBlock() == Blocks.AIR;
  }

  public int getLimiter() {
    return this.blockIndex;
  }
}
