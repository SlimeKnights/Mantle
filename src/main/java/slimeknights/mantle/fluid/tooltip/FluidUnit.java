package slimeknights.mantle.fluid.tooltip;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.primitive.StringLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;

import java.util.List;

/**
 * Single entry for text options
 */
public record FluidUnit(String key, int needed) {
  public static final RecordLoadable<FluidUnit> LOADABLE = RecordLoadable.create(
    StringLoadable.DEFAULT.requiredField("key", FluidUnit::key),
    IntLoadable.FROM_ONE.requiredField("needed", FluidUnit::needed),
    FluidUnit::new);

  /**
   * Gets the display text for this fluid entry
   * @return Display text
   */
  public int getText(List<Component> tooltip, int amount) {
    int full = amount / needed;
    if (full > 0) {
      tooltip.add(Component.translatable(key, full).withStyle(ChatFormatting.GRAY));
    }
    return amount % needed;
  }
}
