package slimeknights.mantle.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;

import javax.annotation.Nullable;
import java.util.List;
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


  /* Texture name */

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
   * Gets the texture name from a stack
   * @param stack  Stack
   * @return  Texture, or empty string if none
   */
  public static String getTextureName(ItemStack stack) {
    return getTextureName(stack.getTag());
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
    return Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(block)).toString();
  }


  /* Texture */

  /**
   * Gets a block for the given name
   * @param name  Block name
   * @return  Block entry, or {@link Blocks#AIR} if no match
   */
  public static Block getBlock(String name) {
    if (!name.isEmpty()) {
      return BuiltInRegistries.BLOCK.get(new ResourceLocation(name));
    }
    return Blocks.AIR;
  }

  /**
   * Gets the texture from a stack
   * @param stack  Stack to fetch texture
   * @return  Texture, or {@link Blocks#AIR} if none
   */
  public static Block getTexture(ItemStack stack) {
    return getBlock(getTextureName(stack));
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
  /**
   * Creates a new item stack with the given block as it's texture tag
   * @param stack  Stack to modify
   * @param name   Block name to set. If empty, clears the tag
   * @return The item stack with the proper NBT
   */
  public static ItemStack setTexture(ItemStack stack, String name) {
    if (!name.isEmpty()) {
      setTexture(stack.getOrCreateTag(), name);
    } else if (stack.hasTag()) {
      setTexture(stack.getTag(), name);
    }
    return stack;
  }

  /**
   * Creates a new item stack with the given block as it's texture tag
   * @param stack Stack to modify
   * @param block Block to set
   * @return The item stack with the proper NBT
   */
  public static ItemStack setTexture(ItemStack stack, @Nullable Block block) {
    if (block == null || block == Blocks.AIR) {
      return setTexture(stack, "");
    }
    return setTexture(stack, BuiltInRegistries.BLOCK.getKey(block).toString());
  }


  /* Block entity */

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


  /* Block */

  /**
   * Adds the texture block to the tooltip
   * @param stack    Stack instance
   * @param tooltip  Tooltip
   */
  public static void addTooltip(ItemStack stack, List<Component> tooltip) {
    Block block = getTexture(stack);
    if (block != Blocks.AIR) {
      tooltip.add(block.getName());
    }
  }

  /**
   * Adds all blocks from the block tag to the specified block for fillItemGroup
   * @param block             Dynamic texture item instance
   * @param tag               Tag for texturing
   * @param list              List of texture blocks
   * @param showAllVariants   If true, shows all variants. If false, shows just the first
   */
  public static void addTagVariants(ItemLike block, TagKey<Item> tag, NonNullList<ItemStack> list, boolean showAllVariants) {
    boolean added = false;
    // using item tags as that is what will be present in the recipe
    for (Holder<Item> candidate : BuiltInRegistries.ITEM.getTagOrEmpty(tag)) {
      if (!candidate.isBound()) {
        continue;
      }
      Item item = candidate.value();
      // Don't add instances of the block itself, see Inspirations enlightened bushes
      if (item == block.asItem()) {
        continue;
      }
      // non-block items don't have the textures we need
      if (!(item instanceof BlockItem blockItem)) {
        continue;
      }
      added = true;
      list.add(RetexturedHelper.setTexture(new ItemStack(block), blockItem.getBlock()));
      if (!showAllVariants) {
        return;
      }
    }
    // if we never got one, just add the textureless one
    if (!added) {
      list.add(new ItemStack(block));
    }
  }
}
