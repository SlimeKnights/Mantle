package slimeknights.mantle.item.tooltip;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import slimeknights.mantle.util.TranslationHelper;

import java.util.List;

/**
 * Armor item with automatic optional tooltip support.
 * @see TooltipItem
 */
@SuppressWarnings("unused")  // API
public class ArmorTooltipItem extends ArmorItem {
  public ArmorTooltipItem(Holder<ArmorMaterial> material, ArmorItem.Type type, Properties builder) {
    super(material, type, builder);
  }

  @Override
  public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    TranslationHelper.addOptionalTooltip(stack, tooltip);
    super.appendHoverText(stack, context, tooltip, flag);
  }
}
