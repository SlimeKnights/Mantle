package slimeknights.mantle.config;

import com.google.common.reflect.TypeToken;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.registries.ForgeRegistries;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;


public class BlockMeta {

  private static final BlockMeta MISSING = new BlockMeta(Blocks.AIR, 0);

  public transient static final TypeSerializer<BlockMeta> SERIALIZER = new TypeSerializer<BlockMeta>() {
    @Override
    public BlockMeta deserialize(TypeToken<?> typeToken, ConfigurationNode configurationNode)
        throws ObjectMappingException {
      String val = configurationNode.getString();
      String[] parts = val.split(":");

      Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(parts[0], parts[1]));
      if(block == Blocks.AIR) {
        return MISSING;
      }
      int meta = -1;
      if(parts.length > 2) {
        meta = Integer.valueOf(parts[2]);
      }

      return new BlockMeta(block, meta);
    }

    @Override
    public void serialize(TypeToken<?> typeToken, BlockMeta blockMeta, ConfigurationNode configurationNode)
        throws ObjectMappingException {
      configurationNode.setValue(blockMeta.toString());
    }
  };

  public Block block;
  public int metadata;

  public BlockMeta() {
  }

  public BlockMeta(Block block, int metadata) {
    this.block = block;
    this.metadata = metadata;
  }

  public static BlockMeta of(BlockState state) {
    return new BlockMeta(state.getBlock(), state.getBlock().getMetaFromState(state));
  }


  @Override
  public boolean equals(Object o) {
    if(this == o) {
      return true;
    }
    if(o == null || getClass() != o.getClass()) {
      return false;
    }

    BlockMeta blockMeta = (BlockMeta) o;

    if(metadata != blockMeta.metadata) {
      return false;
    }
    return block != null ? block.equals(blockMeta.block) : blockMeta.block == null;

  }

  @Override
  public int hashCode() {
    int result = block != null ? block.hashCode() : 0;
    result = 31 * result + metadata;
    return result;
  }

  @Override
  public String toString() {
    String val = block.getRegistryName().toString();
    if(metadata > -1) {
      val += ":" + metadata;
    }
    return val;
  }
}
