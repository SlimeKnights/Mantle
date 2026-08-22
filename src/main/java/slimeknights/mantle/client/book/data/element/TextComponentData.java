package slimeknights.mantle.client.book.data.element;

import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.network.chat.Component;
import slimeknights.mantle.client.book.HTMLUtils;
import slimeknights.mantle.client.book.IHTML;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.util.html.HtmlElement;
import slimeknights.mantle.util.html.HtmlGroup;
import slimeknights.mantle.util.html.HtmlSerializable;

import javax.annotation.Nullable;
import java.util.List;

@Accessors(fluent = true)
@Setter
public class TextComponentData implements IHTML {
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
  public HtmlSerializable toHTML(BookData book) {
    if (text == null) return HtmlSerializable.EMPTY;
    HtmlElement element = HtmlElement.span().add(HTMLUtils.toHtml(text));
    if (dropShadow) {
      element.classes("shadow");
    }
    return element;
  }

  /**
   * Merges TextComponentData[] into a single tag when possible
   *
   * @param list TextComponentData[] to convert
   * @param book parent BookData
   * @return HTML p tag
   */
  public static HtmlSerializable toHTML(@Nullable List<TextComponentData> list, BookData book) {
      if (list == null) return HtmlSerializable.EMPTY;

      boolean prevBreak = false;
      HtmlGroup group = HtmlGroup.indent();
      HtmlElement p = HtmlElement.p();
      group.add(p);

      for (TextComponentData data : list) {
        if (data.isParagraph) {
          if (prevBreak) group.add(HtmlElement.p());
          p = HtmlElement.p();
          group.add(p);
        }

        p.add(data.toHTML(book));

        prevBreak = data.linebreak;
        if (data.linebreak) p.add(HtmlElement.br());
      }
      return group;
  }
}
