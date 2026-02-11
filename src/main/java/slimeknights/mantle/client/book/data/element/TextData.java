package slimeknights.mantle.client.book.data.element;

import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.network.chat.Component;
import slimeknights.mantle.client.book.HTMLUtils;
import slimeknights.mantle.client.book.IHTML;
import slimeknights.mantle.client.book.action.StringActionProcessor;
import slimeknights.mantle.client.book.data.BookData;

import javax.annotation.Nullable;

@Accessors(fluent = true)
@Setter
public class TextData implements IHTML {
  /** @deprecated use {@link #linebreak} */
  @Deprecated(forRemoval = true)
  public static final TextData LINEBREAK = new TextData().linebreak(true);
  public static final String LIST_PREFIX = "• ";
  public static final String LIST_PREFIX_2 = "•\u00a0";

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

  /**
   * Do not use this when working with TextData[] that represents a bulleted list
   * Use {@link #toHTML(TextData[], BookData)} instead
   */
  @Override
  public String toHTML(BookData book) {
    boolean styled = (rgbColor & 0xFFFFFF) != 0 || bold || italic || strikethrough;
    boolean anyStyle = styled || underlined || dropshadow;
    boolean link = !action.isEmpty();

    StringBuilder builder = new StringBuilder();

    if (link) {
      String location = action.substring(action.indexOf(StringActionProcessor.PROTOCOL_SEPARATOR) + StringActionProcessor.PROTOCOL_SEPARATOR.length());
      builder.append("<a href=\"../page-")
        .append(book.findPageNumber(location) / 2)
        .append("/#")
        .append(location)
        .append("\">");
    }

    if (anyStyle) {
      builder.append("<p");

      // underlined and dropshadow checked separately because we use a class for it
      if (underlined || dropshadow) builder.append(" class=\"");
      if (underlined) builder.append("underline ");
      if (dropshadow) builder.append("shadow");
      if (underlined || dropshadow) builder.append("\"");

      if (styled) {
        builder.append(" style=\"");

        if ((rgbColor & 0xFFFFFF) != 0)
          builder.append("color: ")
            .append(HTMLUtils.hexRGB(rgbColor))
            .append(";");
        if (bold) builder.append("font-weight: bold;");
        if (italic) builder.append("font-style: italic;");
        if (strikethrough) builder.append("text-decoration: line-through;");

        builder.append("\"");
      }

      builder.append(">");
    }

    builder.append(HTMLUtils.parse(getText()));

    if (anyStyle) builder.append("</p>");
    if (link) builder.append("</a>");

    return builder.toString();
  }

  /**
   * Merges TextData[] into a single tag when possible
   * Formats any lists with ul tags
   *
   * @param array TextData[] to convert
   * @param book parent BookData
   * @return HTML p and ul tags
   */
  public static String toHTML(@Nullable TextData[] array, BookData book) {
    if (array == null) return "";

    boolean ulOpen = false;
    boolean pOpen = false;
    boolean prevBreak = false;
    StringBuilder builder = new StringBuilder();

    for (TextData data : array) {
      if (data.getText().startsWith(LIST_PREFIX) || data.getText().startsWith(LIST_PREFIX_2)) {
        if (pOpen) {
          pOpen = false;
          builder.append("</p>\n");
        }
        if (!ulOpen) {
          ulOpen = true;
          builder.append("<ul class=\"prop-list\">\n");
        }

        // removes the bullet point character
        data.text = data.getText().replaceFirst(LIST_PREFIX, "").replaceFirst(LIST_PREFIX_2, "");
        builder.append(HTMLUtils.li(data.toHTML(book).replaceAll("<(/?)p>", "<$1span>")));
      } else {
        if (ulOpen) {
          // merges <li> separated by \n
          if (data.getText().equals("\n")) continue;
          ulOpen = false;
          builder.append("</ul>\n");
        }
        if (pOpen) {
          if (data.paragraph) {
            // add an extra p as an extra line
            if (prevBreak) builder.append("</p>\n<p>");
            builder.append("</p>\n<p>");
          }
          if (data.linebreak || data.getText().charAt(data.getText().length() - 1) == '\n') {
            builder.append("<br>");
            prevBreak = true;
          } else {
            prevBreak = false;
          }
        } else {
          pOpen = true;
          builder.append("<p>");
        }

        builder.append(data.toHTML(book).replaceAll("<(/?)p>", "<$1span>"));
      }
    }

    if (ulOpen) builder.append("</ul>");
    if (pOpen) builder.append("</p>");

    return builder.toString();
  }

}
