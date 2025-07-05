package slimeknights.mantle.client.book.data.element;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import slimeknights.mantle.client.book.HTMLUtils;
import slimeknights.mantle.client.book.IHTML;

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

  // TODO: '§'

  public TextData() {
  }

  public TextData(String text) {
    this.text = text;
  }

  // TODO: add other styles
  public String toHTML() {
    Integer rgb = ChatFormatting.getByName(color).getColor();
    return String.format(
      "<span style=\"color: %s%s%s%s%s\"%s>%s</span>",
      HTMLUtils.hexRGB(rgb != null ? rgb : 0),
      bold ? "; font-weight: bold" : "",
      italic ? "; font-style: italic" : "",
      underlined ? "; text-decoration: underline" : "",
      strikethrough ? "; text-decoration: line-through" : "",
      dropshadow ? "class=\"shadow\"" : "",
      text
    );
  }

  // TextData's can merge with others if paragraph == false
  public static String toHTML(TextData[] array) {
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
