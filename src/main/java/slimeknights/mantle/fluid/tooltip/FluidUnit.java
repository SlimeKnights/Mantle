package slimeknights.mantle.fluid.tooltip;

import lombok.RequiredArgsConstructor;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.List;

/**
 * Single entry for text options
 */
@SuppressWarnings("ClassCanBeRecord") // needed in GSON
@RequiredArgsConstructor
public class FluidUnit {

  private final String key;
  private final int needed;

  /**
   * Gets the display text for this fluid entry
   * @return Display text
   */
  public int getText(List<Component> tooltip, int amount) {
    int full = amount / needed;
    if (full > 0) {
      tooltip.add(new TranslatableComponent(key, full).withStyle(ChatFormatting.GRAY));
    }
    return amount % needed;
  }
}
