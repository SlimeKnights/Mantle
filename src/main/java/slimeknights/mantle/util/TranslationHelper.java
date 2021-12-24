package slimeknights.mantle.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.ChatFormatting;
import net.minecraftforge.fml.ForgeI18n;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Helpers for working with translations
 */
@SuppressWarnings("WeakerAccess")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TranslationHelper {

  /**
   * Checks if a key can be translated
   * @param key  Key to check
   * @return  True if its translatable
   */
  public static boolean canTranslate(String key) {
    return !key.equals(ForgeI18n.getPattern(key));
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
    String translated = ForgeI18n.getPattern(key);
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
      tooltip.add(new TextComponent(string).withStyle(ChatFormatting.GRAY));
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
