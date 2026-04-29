package slimeknights.mantle.client.book;


import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import slimeknights.mantle.util.html.HtmlElement;
import slimeknights.mantle.util.html.HtmlGroup;
import slimeknights.mantle.util.html.HtmlSerializable;
import slimeknights.mantle.util.html.HtmlString;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Helpers for converting to HTML.
 * @see HtmlSerializable
 * @see HtmlElement
 */
public class HTMLUtils {
  private static final char COLOR_CHAR = '§';
  private static final String LOOKUP = "0123456789abcdefklmnor";

  /**
   * Parses any chat formatting in to HTML.
   * Does not support Obfuscated §k
   *
   * @param text Minecraft chat formated string
   * @return this as HTML span tag
   */
  public static HtmlSerializable parse(String text) {
    int start = 0;
    int next = text.indexOf(COLOR_CHAR);
    int last = text.length() - 1;
    if (next == -1 || next == last) {
      // does nothing
      return new HtmlString(text);
    }

    // root element to return
    HtmlGroup root = HtmlGroup.inline();
    // current element to append text into
    HtmlGroup current = root;
    HtmlElement span = null;

    do {
      // add text to the current element
      current.add(text.substring(start, next));
      char nextChar = text.charAt(next + 1);
      // if we see a reset, reset all text, so current is now root
      if (nextChar == 'r') {
        span = null;
        current = root;
      } else if (LOOKUP.indexOf(nextChar) != -1) {
        // if we see multiple formatting codes in a row, use a single span
        if (span == null) {
          span = HtmlElement.span();
          current.add(span);
          current = span;
        }
        switch (nextChar) {
          case '0' -> span.color(0x000000);
          case '1' -> span.color(0x0000AA);
          case '2' -> span.color(0x00AA00);
          case '3' -> span.color(0x00AAAA);
          case '4' -> span.color(0xAA0000);
          case '5' -> span.color(0xAA00AA);
          case '6' -> span.color(0xFFAA00);
          case '7' -> span.color(0xAAAAAA);
          case '8' -> span.color(0x555555);
          case '9' -> span.color(0x5555FF);
          case 'a' -> span.color(0x55FF55);
          case 'b' -> span.color(0x55FFFF);
          case 'c' -> span.color(0xFF5555);
          case 'd' -> span.color(0xFF55FF);
          case 'e' -> span.color(0xFFFF55);
          case 'f' -> span.color(0xFFFFFF);
          case 'l' -> span.style("font-weight",     "bold");
          case 'm' -> span.style("text-decoration", "line-through");
          case 'n' -> span.style("text-decoration", "underline");
          case 'o' -> span.style("font-style",      "italic");
        }
        // if we don't have another formatting code, stop adding to this span
        // they will need a new span as text between now and the next one will not want the new formatting
        if (text.charAt(next + 2) != COLOR_CHAR) {
          span = null;
        }
      }
      start = next + 2;
      next = text.indexOf(COLOR_CHAR, start);
    } while (next > -1 && next < last);
    // add remaining text
    current.add(text.substring(start));
    // return the root element where we added all text
    return root;
  }

  /**
   * Decomposes a Component into styled inlined spans
   *
   * @param component Component
   * @return HTML span tag
   */
  public static HtmlSerializable toHtml(Component component) {
    Style style = component.getStyle();
    // going to have a group of elements, just not sure what type yet
    HtmlGroup group;
    if (!style.isEmpty()) {
      // span if we have styles
      HtmlElement element = HtmlElement.span();
      group = element;
      TextColor color = style.getColor();
      if (color != null && (color.getValue() & 0xFFFFFF) != 0) {
        element.color(color.getValue());
      }

      if (style.isBold()) element.style("font-weight", "bold");
      if (style.isItalic()) element.style("font-style", "italic");
      if (style.isStrikethrough()) element.style("text-decoration", "line-through");

      if (style.isUnderlined()) element.classes("underline");
    } else {
      group = HtmlGroup.inline();
    }

    // add contents
    String contents = MutableComponent.create(component.getContents()).getString();
    // if we have newlines, put each element in its own span
    if (contents.indexOf('\n') != -1) {
      group.add(HtmlGroup.indent().add(Arrays.stream(contents.split("\n")).map(str -> HtmlElement.span().add(str))));
    } else {
      group.add(contents);
    }
    // add children
    group.add(component.getSiblings().stream().map(HTMLUtils::toHtml));

    return group;
  }

  /**
   * Merges Strings into an unordered list
   *
   * @param lines Strings, can use Minecraft chat formatting
   * @return li tags
   */
  public static Stream<HtmlSerializable> toListItems(String[] lines) {
    return toListItems(Arrays.stream(lines).map(HTMLUtils::parse));
  }

  /**
   * Merges Components into an unordered list
   *
   * @param lines Components
   * @return li tags
   */
  public static Stream<HtmlSerializable> toListItems(List<Component> lines) {
    return toListItems(lines.stream().map(HTMLUtils::toHtml));
  }

  /**
   * Merges a Stream of HtmlSerializable into an unordered list
   *
   * @param lines HtmlSerializables
   * @return li tags
   */
  public static Stream<HtmlSerializable> toListItems(Stream<HtmlSerializable> lines) {
    return lines.map(line -> HtmlElement.li().add(HtmlElement.p().add(line)));
  }
}
