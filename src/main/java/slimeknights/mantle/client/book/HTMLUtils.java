package slimeknights.mantle.client.book;


import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

import javax.annotation.Nullable;
import java.util.stream.Collectors;

public class HTMLUtils {

  public static String line(String text) {
    return line(text, false);
  }

  public static String line(String text, boolean title) {
    return line(text, title, null);
  }

  public static String line(String text, @Nullable String styles) {
    return line(text, false, styles);
  }

  public static String line(String text, boolean title, @Nullable String styles) {
    return String.format(
      "<p%s%s%s>%s</p>",
      title ? " id=\"" + slugify(text) + "\"" : "",
      title ? " class=\"title\"" : "",
      styles != null ? " style=\"" + styles + "\"" : "",
      text
    );
  }

  public static String line(Component component) {
    return line(toHTML(component));
  }

  /**
   * NOTE: uses a span instead of p to recursively inline
   */
  private static String toHTML(Component component) {
    StringBuilder styleBuilder = new StringBuilder();
    Style style = component.getStyle();
    TextColor color = style.getColor();
    if (color != null && color.getValue() != 0) {
      styleBuilder.append("color: ");
      // toString gives a weird color
      styleBuilder.append(hexRGB(color.getValue()));
      styleBuilder.append(";");
    }

    // TODO: I don't think these are ever used in the in game book
    if (style.isBold()) {}
    if (style.isItalic()) {}
    if (style.isStrikethrough()) {}
    if (style.isUnderlined()) {}

    return String.format("<span style=\"%s\">%s%s</span>", styleBuilder, MutableComponent.create(component.getContents()).getString(), component.getSiblings().stream().map(HTMLUtils::toHTML).collect(Collectors.joining()));
  }

  public static String slugify(String s) {
    return s.toLowerCase().replace(' ', '_');
  }

  public static String hexRGB(int rgb) {
    return String.format("#%02X%02X%02X", (rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, (rgb & 0xFF));
  }

}
