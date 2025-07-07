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

  public TextData() {
  }

  public TextData(String text) {
    this.text = text;
  }

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
      parseChatFormatting()
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

  private static final char COLOR_CHAR = '§';
  private static final String LOOKUP = "0123456789abcdefklmnor";

  // TODO: ignores color for now
  // cant really do Obfuscated §k without client side js
  public String parseChatFormatting() {
    int next = text.indexOf(COLOR_CHAR);
    int last = text.length() - 1;
    if (next == -1 || next == last) {
      // does nothing
      return text;
    }

    int start = 0;
    int left = 0;
    int right = 0;
    boolean open = false;
    StringBuilder result = new StringBuilder();

    do {
      result.append(text, start, next);
      char nextChar = text.charAt(next + 1);
      if (LOOKUP.indexOf(nextChar) != 1 && !open && nextChar != 'r') {
        result.append("<span style=\"");
        open = true;
      };
      switch (nextChar) {
        case 'l' -> result.append("font-weight: bold;");
        case 'm' -> result.append("text-decoration: line-through;");
        case 'n' -> result.append("text-decoration: underline;");
        case 'o' -> result.append("font-style: italic;");
        case 'r' -> { result.append("</span>"); right++; }
      }
      if (nextChar != 'r' && text.charAt(next + 2) != COLOR_CHAR) {
        result.append("\">");
        left++;
        open = false;
      }
      next += 2;
      start = next;
      next = start + text.substring(start).indexOf(COLOR_CHAR);
    } while (next < last && start < next);

    // might not reset
    result.append("</span>".repeat(left - right));

    return result.toString();
  }
}
