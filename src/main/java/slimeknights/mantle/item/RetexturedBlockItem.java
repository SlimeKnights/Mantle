package slimeknights.mantle.item;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.tags.Tag;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.ItemLike;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import slimeknights.mantle.util.RetexturedHelper;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

import net.minecraft.world.item.Item.Properties;

/**
 * Logic for a dynamically retexturable block item. This will ensure all the NBT is in the expected format on the item NBT.
 *
 * Use alongside {@link slimeknights.mantle.tileentity.IRetexturedTileEntity} and {@link slimeknights.mantle.block.RetexturedBlock}
 */
@SuppressWarnings("WeakerAccess")
public class RetexturedBlockItem extends BlockTooltipItem {

  /** Tag used for getting the texture */
  protected final Tag<Item> textureTag;
  public RetexturedBlockItem(Block block, Tag<Item> textureTag, Properties builder) {
    super(block, builder);
    this.textureTag = textureTag;
  }

  @Override
  public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
    if (this.allowdedIn(group)) {
      addTagVariants(this.getBlock(), textureTag, items, true);
    }
  }

  @OnlyIn(Dist.CLIENT)
  @Override
  public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
    super.appendHoverText(stack, worldIn, tooltip, flagIn);
    addTooltip(stack, tooltip);
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
    return setTexture(stack, Objects.requireNonNull(block.getRegistryName()).toString());
  }

  /**
   * Adds all blocks from the block tag to the specified block for fillItemGroup
   * @param block             Dynamic texture item instance
   * @param tag               Tag for texturing
   * @param list              List of texture blocks
   * @param showAllVariants   If true, shows all variants. If false, shows just the first
   */
  public static void addTagVariants(ItemLike block, Tag<Item> tag, NonNullList<ItemStack> list, boolean showAllVariants) {
    boolean added = false;
    // using item tags as that is what will be present in the recipe
    Class<?> clazz = block.getClass();
    if (!ItemTags.getAllTags().getAvailableTags().isEmpty()) {
      for (Item candidate : tag.getValues()) {
        // non-block items don't have the textures we need
        if (!(candidate instanceof BlockItem)) {
          continue;
        }
        Block textureBlock = ((BlockItem)candidate).getBlock();
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
    }
    // if we never got one, just add the textureless one
    if (!added) {
      list.add(new ItemStack(block));
    }
  }
}
