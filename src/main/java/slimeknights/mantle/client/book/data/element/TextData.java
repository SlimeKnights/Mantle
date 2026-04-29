package slimeknights.mantle.client.book.data.element;

import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import slimeknights.mantle.client.book.HTMLUtils;
import slimeknights.mantle.client.book.IHTML;
import slimeknights.mantle.client.book.action.StringActionProcessor;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.util.html.HtmlElement;
import slimeknights.mantle.util.html.HtmlGroup;
import slimeknights.mantle.util.html.HtmlSerializable;

import javax.annotation.Nullable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Accessors(fluent = true)
@Setter
public class TextData implements IHTML {
  /** @deprecated use {@link #linebreak} */
  @Deprecated(forRemoval = true)
  public static final TextData LINEBREAK = new TextData().linebreak(true);
  private static final Pattern LIST_REGEX = Pattern.compile("^\n?(•|\\d+\\.)[ \u00a0]");
  /** Constant to use in mods wishing to implement bulleted lists that are compatible with the book lists. Will also need to use {@link #linebreak(boolean)} */
  public static final String LIST_PREFIX = "•\u00a0";

  // TODO 1.21: make no longer nullable
  @Nullable
  public String text = "";
  public String color = "black";

  public int rgbColor = 0;
  public boolean useOldColor = true;
  public boolean bold = false;
  public boolean italic = false;
  public boolean underlined = false;
  public boolean strikethrough = false;
  public boolean obfuscated = false;
  /** Adds 2 linebreaks before the text */
  public boolean paragraph = false;
  /** If true, adds a line break after the text */
  public boolean linebreak = false;
  public boolean dropshadow = false;
  public float scale = 1;
  public String action = "";
  @Nullable
  public Component[] tooltip = null;

  public TextData(String text) {
    this.text = text;
  }

  public TextData() {
    this("");
  }

  /** Null safe method to get text, as its possible its null due to book parsing. */
  public String getText() {
    return text == null ? "" : text;
  }

  private HtmlSerializable toHTML(BookData book, String rawText) {
    // contains the text, but may be wrapped once or twice
    HtmlSerializable text = HTMLUtils.parse(rawText);

    // apply link
    HtmlElement element = null;
    if (!action.isEmpty()) {
      String location = action.substring(action.indexOf(StringActionProcessor.PROTOCOL_SEPARATOR) + StringActionProcessor.PROTOCOL_SEPARATOR.length());
      element = HtmlElement.a();
      element.add(text);
      element.href("../page-" + book.findPageNumber(location) / 2 + "/#" + location);
      text = element;
    }

    // apply styles
    boolean hasColor = (rgbColor & 0xFFFFFF) != 0 || !color.isEmpty();
    if (hasColor || bold || italic || strikethrough || underlined || dropshadow) {
      if (element == null) {
        // create new element for the style if no link or list item
        element = HtmlElement.span();
        element.add(text);
        text = element;
      }

      // apply styles to the found element
      if (underlined) element.classes("underline");
      if (dropshadow) element.classes("shadow");
      if (hasColor) {
        ChatFormatting formatting = ChatFormatting.getByName(color);
        element.color(formatting != null ? formatting.getColor() : rgbColor);
      }
      if (bold) element.style("font-weight", "bold");
      if (italic) element.style("font-style", "italic");
      if (strikethrough) element.style("text-decoration", "line-through");
    }

    return text;
  }

  /**
   * Do not use this when working with TextData[] that represents a bulleted list
   * Use {@link #toHtml(TextData[], BookData)} instead
   */
  @Override
  public HtmlSerializable toHTML(BookData book) {
    // TODO: should this have a wrapping p?
    return toHTML(book, getText());
  }

  /**
   * Merges TextData[] into a single tag when possible.
   * Formats any lists prefixed with numbers or bullet points.
   *
   * @param array TextData[] to convert
   * @param book parent BookData
   * @return HTML p and ul tags
   */
  public static HtmlSerializable toHtml(@Nullable TextData[] array, BookData book) {
    HtmlGroup group = HtmlGroup.indent();
    if (array == null) return group;

    boolean prevBreak = false;
    @Nullable
    HtmlElement list = null;
    @Nullable
    HtmlElement p = null;

    for (TextData data : array) {
      String text = data.getText();
      Matcher match = LIST_REGEX.matcher(text);
      if (match.find()) {
        if (list == null) {
          list = match.group(1).equals("•") ? HtmlElement.ul().classes("prop-list") : HtmlElement.ol().classes("prop-list");
          group.add(list);
        }
        if (p != null) {
          p = null;
        }
        // removes the bullet point character
        list.add(HtmlElement.li().add(HtmlElement.p().add(
          data.toHTML(book, match.replaceFirst("")))
        ));
      } else {
        if (list != null) {
          // merges <li> separated by \n
          if (text.equals("\n")) continue;
          list = null;
        }
        if (p != null) {
          if (data.paragraph) {
            // add an extra p as an extra line
            if (prevBreak) group.add(HtmlElement.p());
            p = HtmlElement.p();
            group.add(p);
          }
          prevBreak = data.linebreak || text.charAt(text.length() - 1) == '\n';
          if (prevBreak) p.add(HtmlElement.br());
        } else {
          p = HtmlElement.p();
          group.add(p);
        }
        p.add(data.toHTML(book, text));
      }
    }
    return group;
  }
}
