package slimeknights.mantle.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import slimeknights.mantle.util.TranslationHelper;

import java.util.List;

/**
 * Item with automatic tooltip support
 */
public class TooltipItem extends Item {

  public TooltipItem(Properties properties) {
    super(properties);
  }

  @Override
  public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
    TranslationHelper.addOptionalTooltip(stack, tooltip);
    super.appendHoverText(stack, context, tooltip, flagIn);
  }
}
