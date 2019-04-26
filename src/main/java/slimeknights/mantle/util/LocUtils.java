package slimeknights.mantle.util;

import com.google.common.collect.Lists;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

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
    if(text == null)
      return list;
    int j = 0;
    int k;
    while((k = text.indexOf("\\n", j)) >= 0)
    {
      list.add(new TextComponentString(text.substring(j, k)));
      j = k+2;
    }

    list.add(new TextComponentString(text.substring(j, text.length())));

    return list;
  }


  public static String convertNewlines(String line) {
    if(line == null)
      return null;
    int j;
    while((j = line.indexOf("\\n")) >= 0)
    {
      line = line.substring(0, j) + '\n' + line.substring(j+2);
    }

    return line;
  }
}
