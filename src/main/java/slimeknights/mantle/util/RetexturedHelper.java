package slimeknights.mantle.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * This utility contains helpers to handle the NBT for retexturable blocks
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RetexturedHelper {
  /** Tag name for texture blocks. Should not be used directly, use the utils to interact */
  public static final String TAG_TEXTURE = "texture";
  /** Property for tile entities containing a texture block */
  public static final ModelProperty<Block> BLOCK_PROPERTY = new ModelProperty<>(block -> block != Blocks.AIR);


  /* Getting */

  /**
   * Gets a block for the given name
   * @param name  Block name
   * @return  Block entry, or {@link Blocks#AIR} if no match
   */
  public static Block getBlock(String name) {
    if (!name.isEmpty()) {
      return Registry.BLOCK.get(new ResourceLocation(name));
    }
    return Blocks.AIR;
  }

  /**
   * Gets the name of the texture from NBT
   * @param nbt  NBT tag
   * @return  Name of the texture, or empty if no texture
   */
  public static String getTextureName(@Nullable CompoundTag nbt) {
    if (nbt == null) {
      return "";
    }
    return nbt.getString(TAG_TEXTURE);
  }

  /**
   * Gets the name of the texture from the block
   * @param block  Block
   * @return  Name of the texture, or empty if the block is air
   */
  public static String getTextureName(Block block) {
    if (block == Blocks.AIR) {
      return "";
    }
    return Objects.requireNonNull(Registry.BLOCK.getKey(block)).toString();
  }


  /* Setting */

  /**
   * Sets the texture in an NBT instance
   * @param nbt      Tag instance
   * @param texture  Texture to set
   */
  public static void setTexture(@Nullable CompoundTag nbt, String texture) {
    if (nbt != null) {
      if (texture.isEmpty()) {
        nbt.remove(TAG_TEXTURE);
      } else {
        nbt.putString(TAG_TEXTURE, texture);
      }
    }
  }

  /** Helper to call client side when the model data changes to refresh model data */
  public static void onTextureUpdated(BlockEntity self) {
    // update the texture in BE data
    Level level = self.getLevel();
    if (level != null && level.isClientSide) {
      self.requestModelDataUpdate();
      BlockState state = self.getBlockState();
      level.sendBlockUpdated(self.getBlockPos(), state, state, 0);
    }
  }

  /** Creates a builder with the block property as specified */
  public static ModelData.Builder getModelDataBuilder(Block block) {
    // cannot support air, saves a conditional on usage
    if (block == Blocks.AIR) {
      block = null;
    }
    return ModelData.builder().with(BLOCK_PROPERTY, block);
  }

  /** Creates model data with the block property as specified */
  public static ModelData getModelData(Block block) {
    return getModelDataBuilder(block).build();
  }
}
