package slimeknights.mantle.client.book.data.element;

import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.network.chat.Component;
import slimeknights.mantle.client.book.HTMLUtils;
import slimeknights.mantle.client.book.IHTML;
import slimeknights.mantle.client.book.data.BookData;

import javax.annotation.Nullable;
import java.util.List;

@Accessors(fluent = true)
@Setter
public class TextComponentData implements IHTML {
  /** @deprecated use {@link #linebreak} */
  @Deprecated
  public static final TextComponentData LINEBREAK = new TextComponentData((Component) null).linebreak(true);

  @Nullable
  public Component text;

  /** Adds 2 linebreaks before the text */
  public boolean isParagraph = false;
  /** If true, adds a line break after the text */
  public boolean linebreak = false;
  public boolean dropShadow = false;
  public float scale = 1;
  public String action = "";
  @Nullable
  public Component[] tooltips = null;

  public TextComponentData(@Nullable Component text) {
    this.text = text;
  }

  public TextComponentData(String text) {
    this(Component.literal(text));
  }

  @Override
  public String toHTML(BookData book) {
    if (text == null) return "";
    if (dropShadow) return HTMLUtils.p(text, "shadow", null, null);
    return HTMLUtils.p(text);
  }

  /**
   * Merges TextComponentData[] into a single tag when possible
   *
   * @param list TextComponentData[] to convert
   * @param book parent BookData
   * @return HTML p tag
   */
  public static String toHTML(@Nullable List<TextComponentData> list, BookData book) {
      if (list == null) return "";

      boolean prevBreak = false;
      StringBuilder builder = new StringBuilder();
      builder.append("<p>");

      for (TextComponentData data : list) {
        if (data.isParagraph) {
          if (prevBreak) builder.append("</p>\n<p>");
          builder.append("</p><p>");
        }

        //                               replace p with span
        //                               cant just remove it since it might have a shadow class
        builder.append(data.toHTML(book).replaceAll("<(/?)p>", "<$1span>"));

        prevBreak = data.linebreak;
        if (data.linebreak) builder.append("<br>");
      }

      builder.append("</p>");
      return builder.toString();
  }
}
