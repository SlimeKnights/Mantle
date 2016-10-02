package slimeknights.mantle.client.book;

import net.minecraft.item.ItemStack;

public class StructureInfo {
  public ItemStack[][][] data;
  public int blockCount = 0;
  public int[] countPerLevel;
  public int structureHeight = 0;
  public int structureLength = 0;
  public int structureWidth = 0;
  public int showLayer = -1;

  private int blockIndex = -1;
  private int maxBlockIndex;

  public StructureInfo(ItemStack[][][] structure) {
    init(structure);
    maxBlockIndex = blockIndex = structureHeight * structureLength * structureWidth;
  }

  public void init(ItemStack[][][] structure) {
    data = structure;
    structureHeight = structure.length;
    structureWidth = 0;
    structureLength = 0;

    countPerLevel = new int[structureHeight];
    blockCount = 0;
    for(int h = 0; h < structure.length; h++) {
      if(structure[h].length > structureLength) {
        structureLength = structure[h].length;
      }
      int perLvl = 0;
      for(int l = 0; l < structure[h].length; l++) {
        if(structure[h][l].length > structureWidth) {
          structureWidth = structure[h][l].length;
        }
        for(ItemStack ss : structure[h][l]) {
          if(ss != null) {
            perLvl++;
          }
        }
      }
      countPerLevel[h] = perLvl;
      blockCount += perLvl;
    }
  }

  public void setShowLayer(int layer) {
    showLayer = layer;
    blockIndex = (layer + 1) * (structureLength * structureWidth) - 1;
  }

  public void reset() {
    blockIndex = maxBlockIndex;
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

    return data[y][x][z] == null;
  }

  public int getLimiter() {
    return blockIndex;
  }
}
