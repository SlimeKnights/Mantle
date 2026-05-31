package slimeknights.mantle.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.ChatFormatting;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

/**
 * Helpers for working with translations
 */
@SuppressWarnings("WeakerAccess")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TranslationHelper {
  /** Formats a number separated by commas every 3 digits (i.e. US standard) */
  public static final DecimalFormat COMMA_FORMAT = new DecimalFormat("#,###,###.##", DecimalFormatSymbols.getInstance(Locale.US));

  /**
   * Checks if a key can be translated
   * @param key  Key to check
   * @return  True if its translatable
   */
  public static boolean canTranslate(String key) {
    return !key.equals(Language.getInstance().getOrDefault(key));
  }

  /**
   * Better documented way to check if something is translated, use instead of {@link #canTranslate(String)} if you want to reuse the result.
   * @param key        Key to check
   * @param attempted  Attempted translation result
   * @return  True if its translatable
   */
  public static boolean canTranslate(String key, String attempted) {
    return !key.equals(attempted);
  }

  /**
   * Adds localized tooltip to a stack if present
   * @param stack    Stack
   * @param tooltip  List of tooltips
   */
  public static void addOptionalTooltip(ItemStack stack, List<Component> tooltip) {
    addOptionalTooltip(stack.getDescriptionId() + ".tooltip", tooltip);
  }

  /**
   * Adds localized tooltip to a list of tooltips if present
   * @param key      Translation key
   * @param tooltip  List of tooltips
   */
  public static void addOptionalTooltip(String key, List<Component> tooltip) {
    String translated = Language.getInstance().getOrDefault(key);
    if (canTranslate(key, translated)) {
      addEachLine(translated, tooltip);
    }
  }

  /**
   * Adds the text into the tooltip, splitting on newlines
   * @param text     Translated text to split
   * @param tooltip  List of tooltip strings to add to
   */
  public static void addEachLine(String text, List<Component> tooltip) {
    for (String string : text.split("\n")) {
      tooltip.add(Component.literal(string).withStyle(ChatFormatting.GRAY));
    }
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
