package slimeknights.mantle.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

import slimeknights.mantle.util.LocUtils;

// Item with automatic tooltip support
public class ItemTooltip extends Item {

  @Override
  @SideOnly(Side.CLIENT)
  public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
    addOptionalTooltip(stack, tooltip);
    super.addInformation(stack, playerIn, tooltip, advanced);
  }

  public static void addOptionalTooltip(ItemStack stack, List<String> tooltip) {
    if(I18n.canTranslate(stack.getUnlocalizedName() + ".tooltip")) {
      tooltip.addAll(LocUtils.getTooltips(TextFormatting.GRAY.toString() +
                                          LocUtils.translateRecursive(stack.getUnlocalizedName() + ".tooltip")));
    }
    else if(I18n.canTranslate(stack.getUnlocalizedName() + ".tooltip")) {
      tooltip.addAll(LocUtils.getTooltips(
          TextFormatting.GRAY.toString() + LocUtils.translateRecursive(stack.getUnlocalizedName() + ".tooltip")));
    }
  }
}
