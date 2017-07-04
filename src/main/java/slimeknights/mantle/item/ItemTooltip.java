package slimeknights.mantle.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

import javax.annotation.Nullable;

import slimeknights.mantle.util.LocUtils;

// Item with automatic tooltip support
public class ItemTooltip extends Item {

  @Override
  @SideOnly(Side.CLIENT)
  public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
    addOptionalTooltip(stack, tooltip);
    super.addInformation(stack, worldIn, tooltip, flagIn);
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
