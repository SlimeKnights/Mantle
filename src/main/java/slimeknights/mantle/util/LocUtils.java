package slimeknights.mantle.util;

import net.minecraft.util.text.translation.I18n;

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
    return I18n.translateToLocal(I18n.translateToLocalFormatted(key, params));
  }
}
