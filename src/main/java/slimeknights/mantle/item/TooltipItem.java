package slimeknights.mantle.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ForgeI18n;

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
    String translationKey = stack.getTranslationKey() + ".tooltip";

    if (!ForgeI18n.getPattern(translationKey).equals(translationKey)) {
      String translate = ForgeI18n.getPattern(translationKey);
      if (!ForgeI18n.getPattern(translate).equals(translate)) {
        String[] strings = new TranslationTextComponent(translate).getFormattedText().split("\n");

        for (String string : strings) {
          tooltip.add(new StringTextComponent(string).applyTextStyle(TextFormatting.GRAY));
        }
      }
      else {
        String[] strings = new TranslationTextComponent(translationKey).getFormattedText().split("\n");

        for (String string : strings) {
          tooltip.add(new StringTextComponent(string).applyTextStyle(TextFormatting.GRAY));
        }
      }
    }
  }
}
