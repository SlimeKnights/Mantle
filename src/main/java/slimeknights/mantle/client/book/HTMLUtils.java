package slimeknights.mantle.client.book;


import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

import javax.annotation.Nullable;
import java.util.stream.Collectors;

public class HTMLUtils {

  private static final char COLOR_CHAR = '§';
  private static final String LOOKUP = "0123456789abcdefklmnor";

  /**
   * Converts a String into a HTML paragraph
   *
   * @param text text
   * @return HTML p tag
   */
  public static String p(String text) {
    return p(text, null,null, null, null);
  }

  /**
   * Converts a String into a HTML paragraph
   *
   * @param text   text
   * @param styles element style attributes
   * @return HTML p tag
   */
  public static String p(String text, String styles) {
    return p(text, null,null, null, styles);
  }

  /**
   * Converts a String into a HTML paragraph
   *
   * @param text    text
   * @param classes element classes
   * @param tooltip text tooltip
   * @param styles  element style attributes
   * @return HTML p tag
   */
  public static String p(String text, @Nullable String classes, @Nullable String tooltip, @Nullable String styles) {
    return html("p", text, null, classes, tooltip, styles);
  }

  /**
   * Converts a String into a HTML paragraph
   *
   * @param text    text
   * @param id      element id
   * @param classes element classes
   * @param tooltip text tooltip
   * @param styles  element style attributes
   * @return HTML p tag
   */
  public static String p(String text, @Nullable String id, @Nullable String classes, @Nullable String tooltip, @Nullable String styles) {
    return html("p", text, id, classes, tooltip, styles);
  }

  /**
   * Converts a String into a HTML list item
   *
   * @param text text
   * @return HTML li tag
   */
  public static String li(String text) {
    return li(text, null, null, null);
  }

  /**
   * Converts a String into a HTML list item
   *
   * @param text    text
   * @param classes element classes
   * @param tooltip text tooltip
   * @param styles  element style attributes
   * @return HTML li tag
   */
  public static String li(String text, @Nullable String classes, @Nullable String tooltip, @Nullable String styles) {
    // nesting li in a p seems to fix inconsistent spacing between lines
    return html("li", p(text, null, classes, tooltip, styles), null, null, null, null);
  }

  /**
   * Converts a String into HTML
   *
   * @param tag     HTML tag
   * @param text    text
   * @param id      element id
   * @param classes element classes
   * @param tooltip text tooltip
   * @param styles  element style attributes
   * @return arbitrary HTML tag
   */
  private static String html(String tag, String text, @Nullable String id, @Nullable String classes, @Nullable String tooltip, @Nullable String styles) {
    StringBuilder builder = new StringBuilder("<").append(tag);

    if (id != null) builder.append(" id=\"").append(id).append("\"");
    if (classes != null) builder.append(" class=\"").append(classes).append("\"");
    if (tooltip != null) builder.append(" data-minetip-title='").append(tooltip).append("'");
    if (styles != null) builder.append(" style=\"").append(styles).append("\"");

    return builder.append(">")
      .append(text)
      .append("</").append(tag).append(">")
      .toString();
  }

  /**
   * Converts a Component into a HTML paragraph
   * all of its styles are included
   *
   * @param component Component
   * @return HTML p tag
   */
  public static String p(Component component) {
    return p(span(component));
  }

  /**
   * Converts a Component into a HTML paragraph
   * all of its styles are included
   *
   * @param component Component
   * @param styles    element style attributes
   * @return HTML p tag
   */
  public static String p(Component component, String styles) {
    return p(span(component), styles);
  }

  /**
   * Converts a Component into a HTML paragraph
   *
   * @param component Component
   * @param classes   element classes
   * @param tooltip   text tooltip
   * @param styles    element style attributes
   * @return HTML p tag
   */
  public static String p(Component component, @Nullable String classes, @Nullable String tooltip, @Nullable String styles) {
    return html("p", span(component), null, classes, tooltip, styles);
  }

  /**
   * Converts a Component into a HTML list item
   * all of its styles are included
   *
   * @param component Component
   * @return HTML li tag
   */
  public static String li(Component component) {
    return li(span(component));
  }

  /**
   * Decomposes a Component into styled inlined spans
   *
   * @param component Component
   * @return HTML span tag
   */
  private static String span(Component component) {
    StringBuilder builder = new StringBuilder();

    Style style = component.getStyle();
    if (!style.isEmpty()) {
      builder.append("<span style=\"");

      TextColor color = style.getColor();
      if (color != null && (color.getValue() & 0xFFFFFF) != 0) {
        builder.append("color: ");
        builder.append(hexRGB(color.getValue()));
        builder.append(";");
      }

      if (style.isBold()) builder.append("font-weight: bold;");
      if (style.isItalic()) builder.append("font-style: italic;");
      if (style.isStrikethrough()) builder.append("text-decoration: line-through;");

      builder.append("\"");

      if (style.isUnderlined()) builder.append(" class=\"underline\"");

      builder.append(">");
    }

    builder.append(MutableComponent.create(component.getContents()).getString())
      .append(component.getSiblings().stream().map(HTMLUtils::span).collect(Collectors.joining()));

    if (!style.isEmpty()) builder.append("</span>");

    return builder.toString();
  }

  public static String hexRGB(int rgb) {
    return String.format("#%06X", rgb & 0xFFFFFF);
  }

  /**
   * Parses any chat formatting in to HTML.
   * Does not support Obfuscated §k
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
    // might not reset style
    result.append("</span>".repeat(left - right));

    return result.toString();
  }
}
