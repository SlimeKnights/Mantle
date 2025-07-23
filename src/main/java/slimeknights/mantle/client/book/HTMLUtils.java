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
    return s.toLowerCase()
      .replace(' ', '-')
      .replace('_', '-');
  }

  public static String hexRGB(int rgb) {
    return String.format("#%02X%02X%02X", (rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, (rgb & 0xFF));
  }

  private static final char COLOR_CHAR = '§';
  private static final String LOOKUP = "0123456789abcdefklmnor";

  // we can't really do Obfuscated §k without client side js
  /**
   * Parses any chat formatting in text, and converts it to HTML
   *
   * @param text Minecraft chat formated string
   * @return this as HTML span tag
   */
  public static String parse(String text) {
    int start = 0;
    int next = text.indexOf(COLOR_CHAR);
    int last = text.length() - 1;
    if (next == -1 || next == last) {
      // does nothing
      return text;
    }

    int left = 0;
    int right = 0;
    boolean open = false;
    StringBuilder result = new StringBuilder();

    do {
      result.append(text, start, next);
      char nextChar = text.charAt(next + 1);
      if (LOOKUP.indexOf(nextChar) != -1 && !open && nextChar != 'r') {
        result.append("<span style=\"");
        open = true;
      }
      switch (nextChar) {
        case '0' -> result.append("color: #000000;");
        case '1' -> result.append("color: #0000AA;");
        case '2' -> result.append("color: #00AA00;");
        case '3' -> result.append("color: #00AAAA;");
        case '4' -> result.append("color: #AA0000;");
        case '5' -> result.append("color: #AA00AA;");
        case '6' -> result.append("color: #FFAA00;");
        case '7' -> result.append("color: #AAAAAA;");
        case '8' -> result.append("color: #555555;");
        case '9' -> result.append("color: #5555FF;");
        case 'a' -> result.append("color: #55FF55;");
        case 'b' -> result.append("color: #55FFFF;");
        case 'c' -> result.append("color: #FF5555;");
        case 'd' -> result.append("color: #FF55FF;");
        case 'e' -> result.append("color: #FFFF55;");
        case 'f' -> result.append("color: #FFFFFF;");
        case 'l' -> result.append("font-weight: bold;");
        case 'm' -> result.append("text-decoration: line-through;");
        case 'n' -> result.append("text-decoration: underline;");
        case 'o' -> result.append("font-style: italic;");
        case 'r' -> {
          result.append("</span>");
          right++;
        }
      }
      if (nextChar != 'r' && text.charAt(next + 2) != COLOR_CHAR) {
        result.append("\">");
        left++;
        open = false;
      }
      next += 2;
      start = next;
      next += text.substring(start).indexOf(COLOR_CHAR);
    } while (next < last && start <= next);

    result.append(text, start, text.length());
    // might not reset
    result.append("</span>".repeat(left - right));

    return result.toString();
  }
}
