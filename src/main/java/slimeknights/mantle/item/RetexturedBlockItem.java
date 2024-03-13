package slimeknights.mantle.item;

import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ItemLike;
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
  public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
    if (this.allowedIn(group)) {
      addTagVariants(this.getBlock(), textureTag, items, true);
    }
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
    return setTexture(stack, Registry.BLOCK.getKey(block).toString());
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
    Class<?> clazz = block.getClass();
    for (Holder<Item> candidate : Registry.ITEM.getTagOrEmpty(tag)) {
      if (!candidate.isBound()) {
        continue;
      }
      // non-block items don't have the textures we need
      Item item = candidate.value();
      if (!(item instanceof BlockItem)) {
        continue;
      }
      Block textureBlock = ((BlockItem)item).getBlock();
      // Don't add instances of the block itself, see Inspirations enlightened bushes
      if (clazz.isInstance(textureBlock)) {
        continue;
      }
      added = true;
      list.add(setTexture(new ItemStack(block), textureBlock));
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
