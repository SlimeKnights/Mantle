package slimeknights.mantle.item;

import net.minecraft.network.chat.Component;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import slimeknights.mantle.util.TranslationHelper;

import java.util.List;

public class ArmorTooltipItem extends ArmorItem {

  public ArmorTooltipItem(Holder<ArmorMaterial> armorMaterial, ArmorItem.Type type, Properties builder) {
    super(armorMaterial, type, builder);
  }

  @Override
  public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
    TranslationHelper.addOptionalTooltip(stack, tooltip);
    super.appendHoverText(stack, context, tooltip, flagIn);
  }
}
