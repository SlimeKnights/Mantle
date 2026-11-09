package slimeknights.mantle.item.tooltip;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

import java.util.List;

/**
 * Adds a tooltip to an item using the stack sensitive {@link ItemStack#getDescriptionId()} as the base for the tooltip key.
 * Like {@link OptionalTooltipItem}, automatically applies the tooltip without an extra constructor parameter, making it easier to extend for known fixed tooltips. However, does not split the tooltip over newlines.
 * If the tooltip is always present and not stack sensitive, use {@link FixedTooltipItem} which caches the tooltip.
 * For {@link net.minecraft.world.item.BlockItem}, see {@link FixedTooltipBlockItem(Block, Properties)} which does the same apart from stack sensitive.
 */
public abstract class StackTooltipItem extends Item {
  public StackTooltipItem(Properties properties) {
    super(properties);
  }

  @Override
  public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltips, TooltipFlag flag) {
    tooltips.add(FixedTooltipItem.makeComponent(stack.getDescriptionId() + ".tooltip"));
  }
}
