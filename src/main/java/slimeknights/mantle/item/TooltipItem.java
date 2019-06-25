package slimeknights.mantle.item;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import slimeknights.mantle.util.LocUtils;

import javax.annotation.Nullable;
import java.util.List;

// Item with automatic tooltip support
public class TooltipItem extends Item {

  public TooltipItem(Properties properties) {
    super(properties);
  }

  @Override
  @OnlyIn(Dist.CLIENT)
  public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
    addOptionalTooltip(stack, tooltip);
    super.addInformation(stack, worldIn, tooltip, flagIn);
  }

  public static void addOptionalTooltip(ItemStack stack, List<ITextComponent> tooltip) {
    if (I18n.hasKey(stack.getDisplayName() + ".tooltip")) {
      tooltip.addAll(LocUtils.getTooltips(TextFormatting.GRAY.toString() +
              LocUtils.translateRecursive(stack.getDisplayName() + ".tooltip")));
    }
    else if (I18n.hasKey(stack.getDisplayName() + ".tooltip")) {
      tooltip.addAll(LocUtils.getTooltips(
              TextFormatting.GRAY.toString() + LocUtils.translateRecursive(stack.getDisplayName() + ".tooltip")));
    }
  }
}
