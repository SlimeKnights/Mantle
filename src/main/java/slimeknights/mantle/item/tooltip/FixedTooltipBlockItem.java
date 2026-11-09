package slimeknights.mantle.item.tooltip;

import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

import java.util.Arrays;
import java.util.List;

/**
 * Item with automatic mandatory tooltip support.
 * @see FixedTooltipItem
 * @see OptionalTooltipBlockItem
 */
public class FixedTooltipBlockItem extends BlockItem {
  private final List<Component> tooltip;
  public FixedTooltipBlockItem(List<Component> tooltip, Block block, Properties builder) {
    super(block, builder);
    this.tooltip = tooltip;
  }

  /** Creates a block with the default tooltip component. Equivalent to {@link #fromBlock(Block, Properties)}. */
  public FixedTooltipBlockItem(Block block, Properties builder) {
    super(block, builder);
    this.tooltip = List.of(makeComponent(block));
  }

  /** Makes a component for a tooltip from the given ID with the proper styling */
  public static Component makeComponent(Block block) {
    return FixedTooltipItem.makeComponent(block.getDescriptionId() + ".tooltip");
  }

  /** Creates a new block item from the given components */
  public static FixedTooltipBlockItem fromComponents(Block block, Properties properties, Component... tooltip) {
    return new FixedTooltipBlockItem(List.of(tooltip), block, properties);
  }

  /** Creates a new block item from the given translation key for the tooltip */
  public static FixedTooltipBlockItem fromKey(String key, Block block, Properties properties) {
    return fromComponents(block, properties, FixedTooltipItem.makeComponent(key));
  }

  /** Creates a new block item from the given registry ID for the tooltip prefix. Same as {@link FixedTooltipBlockItem(Block,Properties)} but as a static method to mirror the standard item. */
  public static FixedTooltipBlockItem fromBlock(Block block, Properties properties) {
    return new FixedTooltipBlockItem(block, properties);
  }

  /** Creates a new block item from the given registry ID for the tooltip prefix */
  public static FixedTooltipBlockItem fromSuffixes(Block block, Properties properties, ResourceLocation id, String... suffixes) {
    String root = Util.makeDescriptionId("block", id) + '.';
    return new FixedTooltipBlockItem(Arrays.stream(suffixes).map(suffix -> FixedTooltipItem.makeComponent(root + suffix)).toList(), block, properties);
  }

  @Override
  public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    super.appendHoverText(stack, context, tooltip, flag);
    tooltip.addAll(this.tooltip);
  }
}
