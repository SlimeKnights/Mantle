package slimeknights.mantle.client.book;


import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

import javax.annotation.Nullable;
import java.util.stream.Collectors;

public class HTMLUtils {

  /**
   * Converts a String into HTML
   *
   * @param text   text
   * @param styles HTML style attributes
   * @return HTML p tag
   */
  public static String line(String text, String... styles) {
    return line(text, false, styles);
  }

  /**
   * Converts a String into HTML
   *
   * @param text      text
   * @param underline underlines text
   * @param styles    HTML style attributes
   * @return HTML p tag
   */
  public static String line(String text, boolean underline, String... styles) {
    return line(text, null, underline, false, styles);
  }

  /**
   * Converts a String into HTML
   *
   * @param text      text
   * @param underline underlines text
   * @param large     20px font size instead of 13px
   * @param styles    HTML style attributes
   * @return HTML p tag
   */
  public static String line(String text, boolean underline, boolean large, String... styles) {
    return line(text, null, underline, large, styles);
  }

  /**
   * Converts a String into HTML
   *
   * @param text      text
   * @param id        tag id
   * @param underline underlines text
   * @param large     20px font size instead of 13px
   * @param styles    HTML style attributes
   * @return HTML p tag
   */
  public static String line(String text, @Nullable String id, boolean underline, boolean large, String... styles) {
    StringBuilder builder = new StringBuilder("<p");

    if (id != null) builder.append(" id=\"").append(id).append("\"");

    if (underline || large) builder.append(" class=\"");
    if (underline) builder.append("underline ");
    if (large) builder.append("large");
    if (underline || large) builder.append("\"");

    if (styles.length > 0) builder.append(" style=\"").append(String.join("; ", styles)).append("\"");

    return builder.append(">")
      .append(text)
      .append("</p>")
      .toString();
  }

  /**
   * Converts a Component into HTML, all of its styles are included
   *
   * @param component component
   * @return HTML p tag
   */
  public static String line(Component component) {
    return line(toHTML(component));
  }

  /**
   * NOTE: uses a span instead of p to recursively inline
   */
  private static String toHTML(Component component) {
    StringBuilder builder = new StringBuilder();

    Style style = component.getStyle();
    if (!style.isEmpty()) {
      builder.append("<span style=\"");

      TextColor color = style.getColor();
      if (color != null && (color.getValue() & 0xFFFFFF) != 0) {
        builder.append("color: ");
        // toString gives a weird color
        builder.append(hexRGB(color.getValue()));
        builder.append(";");
      }

      if (style.isBold()) builder.append("font-weight: bold;");
      if (style.isItalic()) builder.append("font-style: italic;");
      if (style.isStrikethrough()) builder.append("text-decoration: line-through;");

      if (style.isUnderlined()) builder.append(" class=\"underline\"");
      builder.append("\">");
    }

    builder.append(MutableComponent.create(component.getContents()).getString())
      .append(component.getSiblings().stream().map(HTMLUtils::toHTML).collect(Collectors.joining()));

    if (!style.isEmpty()) builder.append("</span>");

    return builder.toString();
  }

  public static String slugify(String s) {
    return s.toLowerCase().replace(' ', '_');
  }

  public static String hexRGB(int rgb) {
    return String.format("#%02X%02X%02X", (rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, (rgb & 0xFF));
  }
}
