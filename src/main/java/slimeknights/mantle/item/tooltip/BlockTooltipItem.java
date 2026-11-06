package slimeknights.mantle.item.tooltip;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import slimeknights.mantle.registration.deferred.BlockItemBuilder;
import slimeknights.mantle.util.TranslationHelper;

import java.util.List;

/**
 * Item with automatic optional tooltip support.
 * Note its possible to do the same thing though the block using {@link TranslationHelper#addOptionalTooltip(ItemStack, List)}.
 * TODO: rename to {@code OptionalTooltipBlockItem} once its no longer referenced directly in our mods.
 * @see OptionalTooltipItem
 * @see FixedTooltipBlockItem
 * @deprecated use {@link BlockItemBuilder#optionalBlockTooltip()} or {@link BlockItemBuilder#OPTIONAL_TOOLTIP_ITEM)}.
 */
@Deprecated
public class BlockTooltipItem extends BlockItem {
  public BlockTooltipItem(Block blockIn, Item.Properties builder) {
    super(blockIn, builder);
  }

  @Override
  public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    super.appendHoverText(stack, context, tooltip, flag);
    TranslationHelper.addOptionalTooltip(stack, tooltip);
  }
}
