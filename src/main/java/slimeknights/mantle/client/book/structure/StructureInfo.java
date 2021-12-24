package slimeknights.mantle.client.book.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

//Stolen back from Blu stealing back boni's stolen code
public class StructureInfo implements Predicate<BlockPos> {

  public Map<BlockPos, StructureBlockInfo> data = new HashMap<>();
  public final int structureHeight;
  public final int structureLength;
  public final int structureWidth;
  public final int maxBlockIndex;

  public int showLayer = -1;
  public int blockIndex;

  public StructureInfo(List<StructureBlockInfo> structure) {
    int structureHeight = 0;
    int structureWidth = 0;
    int structureLength = 0;

    for (StructureBlockInfo block : structure) {
      structureHeight = Math.max(structureHeight, block.pos.getY() + 1);
      structureWidth = Math.max(structureWidth, block.pos.getZ() + 1);
      structureLength = Math.max(structureLength, block.pos.getX() + 1);
      data.put(block.pos, block);
    }

    this.maxBlockIndex = this.blockIndex = structureHeight * structureLength * structureWidth;
    this.structureHeight = structureHeight;
    this.structureLength = structureLength;
    this.structureWidth = structureWidth;
  }

  public void setShowLayer(int layer) {
    showLayer = layer;

    if (layer < 0)
      this.reset();
    else
      this.blockIndex = (layer + 1) * (this.structureLength * this.structureWidth) - 1;
  }

  public void reset() {
    this.blockIndex = this.maxBlockIndex;
  }

  public void step() {
    final int start = this.blockIndex;

    do {
      if (++this.blockIndex >= this.maxBlockIndex)
        this.blockIndex = 0;
    }
    while (this.isEmpty(this.blockIndex) && this.blockIndex != start);
  }

  private boolean isEmpty(int index) {
    int y = index / (this.structureLength * this.structureWidth);
    int r = index % (this.structureLength * this.structureWidth);
    int x = r / this.structureWidth;
    int z = r % this.structureWidth;

    return !this.data.containsKey(new BlockPos(x, y, z));
  }

  public int getLimiter() {
    return this.blockIndex;
  }

  @Override
  public boolean test(BlockPos blockPos) {
    int index = blockPos.getZ() + this.structureWidth * (blockPos.getX() + this.structureLength * blockPos.getY());
    return index <= this.getLimiter();
  }
}
