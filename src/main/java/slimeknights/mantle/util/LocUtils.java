package slimeknights.mantle.util;

import com.google.common.collect.Lists;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.ForgeI18n;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;

// localization utils
public abstract class LocUtils {

  private LocUtils() {
  }

  /**
   * Removes all whitespaces from the string and makes it lowercase.
   */
  public static String makeLocString(String unclean) {
    return unclean.toLowerCase(Locale.US).replaceAll(" ", "");
  }

  public static String translateRecursive(String key, Object... params) {
    return I18n.format(I18n.format(key, params));
  }

  public static List<ITextComponent> getTooltips(String text) {
    List<ITextComponent> list = Lists.newLinkedList();
    if (!ForgeI18n.getPattern(text).equals(text)) {
      String translate = ForgeI18n.getPattern(text);
      if (!ForgeI18n.getPattern(translate).equals(translate)) {
        String[] strings = new TranslationTextComponent(translate).getString().split("\n");

        for (String string : strings) {
          list.add(new StringTextComponent(string).mergeStyle(TextFormatting.GRAY));
        }
      }
      else {
        String[] strings = new TranslationTextComponent(text).getString().split("\n");

        for (String string : strings) {
          list.add(new StringTextComponent(string).mergeStyle(TextFormatting.GRAY));
        }
      }
    }

    return list;
  }

  @Nullable
  public static String convertNewlines(@Nullable String line) {
    if (line == null) {
      return null;
    }
    int j;
    while ((j = line.indexOf("\\n")) >= 0) {
      line = line.substring(0, j) + '\n' + line.substring(j + 2);
    }

    return line;
  }
}
