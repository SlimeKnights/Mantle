package slimeknights.mantle.client.book.data.element;

import net.minecraft.network.chat.Component;
import slimeknights.mantle.client.book.HTMLUtils;
import slimeknights.mantle.client.book.IHTML;

import javax.annotation.Nullable;

public class TextData implements IHTML {

  public static final TextData LINEBREAK = new TextData("\n");

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

  /** Do not use this directly on TextData[] */
  public String toHTML() {
    if (text.equals("\n")) return "<br>";

    boolean styled = (rgbColor & 0xFFFFFF) != 0 || bold || italic || strikethrough;
    boolean any = styled || underlined || dropshadow;

    StringBuilder builder = new StringBuilder();
    if (any) {
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

    if (any) builder.append("</span>");

    return builder.toString();
  }

  // TextData's can merge with others if paragraph == false
  public static String toHTML(@Nullable TextData[] array) {
    if (array == null) return "";
    StringBuilder builder = new StringBuilder("<p>");
    for (TextData textData : array) {
      if (textData.paragraph) {
        builder.append("</p>\n<p>");
      } else {
        builder.append("\n");
      }
      builder.append(textData.toHTML());
    }
    builder.append("</p>");
    return builder.toString();
  }

}
