package slimeknights.mantle.item.tooltip;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import slimeknights.mantle.util.TranslationHelper;

import java.util.List;

/**
 * Item with automatic optional tooltip support.
 * @see TranslationHelper#addOptionalTooltip(ItemStack, List)
 * @see FixedTooltipItem
 * @see OptionalTooltipBlockItem
 * @deprecated use {@link slimeknights.mantle.registration.deferred.ItemDeferredRegister#registerOptionalTooltip(String, Properties)} or {@link OptionalTooltipItem}
 */
@Deprecated(forRemoval = true)
public class TooltipItem extends Item {
  public TooltipItem(Properties properties) {
    super(properties);
  }

  @Override
  public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
    TranslationHelper.addOptionalTooltip(stack, tooltip);
    super.appendHoverText(stack, context, tooltip, flagIn);
  }
}
