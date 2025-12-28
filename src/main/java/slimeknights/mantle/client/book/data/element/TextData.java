package slimeknights.mantle.client.book.data.element;

import net.minecraft.network.chat.Component;
import slimeknights.mantle.client.book.HTMLUtils;
import slimeknights.mantle.client.book.IHTML;
import slimeknights.mantle.client.book.action.StringActionProcessor;
import slimeknights.mantle.client.book.data.BookData;

import javax.annotation.Nullable;

public class TextData implements IHTML {

  public static final TextData LINEBREAK = new TextData("\n");
  private static final String LIST_PREFIX = "• ";
  public String text;
  public String color = "black";
  public int rgbColor = 0;
  public boolean useOldColor = true;
  public boolean bold = false;
  public boolean italic = false;
  public boolean underlined = false;
  public boolean strikethrough = false;
  public boolean obfuscated = false;
  public boolean paragraph = false;
  public boolean dropshadow = false;
  public float scale = 1.F;
  public String action = "";
  public Component[] tooltip = null;

  public TextData() {
  }

  public TextData(String text) {
    this.text = text;
  }

  /**
   * Do not use this when working with TextData[]
   * Use {@link #toHTML(TextData[], BookData)} instead
   */
  @Override
  public String toHTML() {
    return toHTML(null);
  }

  public String toHTML(@Nullable BookData book) {
    boolean styled = (rgbColor & 0xFFFFFF) != 0 || bold || italic || strikethrough;
    boolean anyStyle = styled || underlined || dropshadow;
    boolean link = !action.isEmpty() && book != null;

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
      builder.append("<span");

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

        builder.append("\">");
      }
    }

    builder.append(HTMLUtils.parse(this.text));

    if (anyStyle) builder.append("</span>");
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
  public static String toHTML(@Nullable TextData[] array, @Nullable BookData book) {
    if (array == null) return "";

    boolean ulOpen = false;
    boolean pOpen = false;
    StringBuilder builder = new StringBuilder();

    for (TextData textData : array) {
      if (textData.text.strip().startsWith(LIST_PREFIX)) {
        if (pOpen) {
          pOpen = false;
          builder.append("</p>\n");
        }
        if (!ulOpen) {
          ulOpen = true;
          builder.append("<ul class=\"prop-list\">\n");
        }

        // removes the bullet point character
        String cleaned = textData.text.strip().replaceFirst(LIST_PREFIX, "");
        TextData temp = new TextData(cleaned);
        temp.rgbColor = textData.rgbColor;
        temp.useOldColor = textData.useOldColor;
        temp.bold = textData.bold;
        temp.italic = textData.italic;
        temp.underlined = textData.underlined;
        temp.strikethrough = textData.strikethrough;
        temp.dropshadow = textData.dropshadow;
        builder.append("<li>").append(temp.toHTML(book)).append("</li>\n");
      } else {
        if (ulOpen) {
          // merges <li> separated by \n
          if (textData.text.equals("\n")) continue;
          ulOpen = false;
          builder.append("</ul>\n");
        }
        if (pOpen) {
          if (textData.paragraph) builder.append("</p>\n<p>");
          else if (textData.text.charAt(0) == '\n') builder.append("<br>");
        } else {
          pOpen = true;
          builder.append("<p>");
        }

        builder.append(textData.toHTML());
      }
    }

    if (ulOpen) builder.append("</ul>");
    if (pOpen) builder.append("</p>");

    return builder.toString();
  }

}
