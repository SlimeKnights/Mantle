package slimeknights.mantle.item.tooltip;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.Arrays;
import java.util.List;

/**
 * Item with automatic mandatory tooltip support.
 * @see OptionalTooltipItem
 * @see FixedTooltipBlockItem
 */
public class FixedTooltipItem extends Item {
  private final List<Component> tooltip;
  public FixedTooltipItem(List<Component> tooltip, Properties properties) {
    super(properties);
    this.tooltip = tooltip;
  }

  /** Creates a new block item from the given components */
  public static FixedTooltipItem fromComponents(Properties properties, Component... tooltip) {
    return new FixedTooltipItem(List.of(tooltip), properties);
  }

  /** Creates a new item from the given translation key for the tooltip */
  public static FixedTooltipItem fromKey(String key, Properties properties) {
    return fromComponents(properties, Component.translatable(key).withStyle(ChatFormatting.GRAY));
  }

  /** Creates a new item from the given registry ID */
  public static FixedTooltipItem fromId(ResourceLocation id, Properties properties) {
    return fromKey(Util.makeDescriptionId("item", id) + ".tooltip", properties);
  }

  /** Creates a new item from the given registry ID */
  public static FixedTooltipItem fromSuffixes(Properties properties, ResourceLocation id, String... suffixes) {
    String root = Util.makeDescriptionId("item", id) + '.';
    return new FixedTooltipItem(Arrays.stream(suffixes).<Component>map(suffix -> Component.translatable(root + suffix).withStyle(ChatFormatting.GRAY)).toList(), properties);
  }

  @Override
  public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    tooltip.addAll(this.tooltip);
  }
}
