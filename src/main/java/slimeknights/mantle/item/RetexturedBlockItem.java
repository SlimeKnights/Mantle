package slimeknights.mantle.item;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import slimeknights.mantle.block.RetexturedBlock;
import slimeknights.mantle.block.entity.IRetexturedBlockEntity;
import slimeknights.mantle.util.RetexturedHelper;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Logic for a dynamically retexturable block item. This will ensure all the NBT is in the expected format on the item NBT.
 *
 * Use alongside {@link IRetexturedBlockEntity} and {@link RetexturedBlock}
 */
@SuppressWarnings("WeakerAccess")
public class RetexturedBlockItem extends BlockTooltipItem {

  /** Tag used for getting the texture */
  protected final TagKey<Item> textureTag;
  public RetexturedBlockItem(Block block, TagKey<Item> textureTag, Properties builder) {
    super(block, builder);
    this.textureTag = textureTag;
  }

  @Override
  public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
    addTooltip(stack, tooltip);
    super.appendHoverText(stack, worldIn, tooltip, flagIn);
  }


  /* Utils */

  /**
   * Gets the texture name from a stack
   * @param stack  Stack
   * @return  Texture, or empty string if none
   */
  public static String getTextureName(ItemStack stack) {
    return RetexturedHelper.getTextureName(stack.getTag());
  }

  /**
   * Gets the texture from a stack
   * @param stack  Stack to fetch texture
   * @return  Texture, or {@link Blocks#AIR} if none
   */
  public static Block getTexture(ItemStack stack) {
    return RetexturedHelper.getBlock(getTextureName(stack));
  }

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
   * Creates a new item stack with the given block as it's texture tag
   * @param stack  Stack to modify
   * @param name   Block name to set. If empty, clears the tag
   * @return The item stack with the proper NBT
   */
  public static ItemStack setTexture(ItemStack stack, String name) {
    if (!name.isEmpty()) {
      RetexturedHelper.setTexture(stack.getOrCreateTag(), name);
    } else if (stack.hasTag()) {
      RetexturedHelper.setTexture(stack.getTag(), name);
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

}
