package slimeknights.mantle.client.book.data.content;

import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.data.element.ImageData;
import slimeknights.mantle.client.book.data.element.TextData;
import slimeknights.mantle.client.screen.book.BookScreen;
import slimeknights.mantle.client.screen.book.element.BookElement;
import slimeknights.mantle.client.screen.book.element.ImageElement;
import slimeknights.mantle.client.screen.book.element.TextElement;
import slimeknights.mantle.util.html.HtmlElement;
import slimeknights.mantle.util.html.HtmlGroup;
import slimeknights.mantle.util.html.HtmlSerializable;

import java.util.ArrayList;

public class ContentTextLeftImage extends PageContent {
  public static final ResourceLocation ID = Mantle.getResource("text_left_image");

  @Getter
  public String title = null;
  public ImageData image;

  // TODO: rename these fields in 1.21 to right_text, and bottom_text
  public TextData[] text1;
  public TextData[] text2;

  private final int OFFSET = 55;

  @Override
  public void build(BookData book, ArrayList<BookElement> list, boolean rightSide) {
    int y = getTitleHeight();

    if (this.title == null || this.title.isEmpty()) {
      y = 0;
    } else {
      this.addTitle(list, this.title);
    }

    if (this.image != null && this.image.location != null) {
      list.add(new ImageElement(0, y, 50, 50, this.image));
    } else {
      list.add(new ImageElement(0, y, 50, 50, ImageData.MISSING));
    }

    if (this.text1 != null && this.text1.length > 0) {
      list.add(new TextElement(OFFSET, y, BookScreen.PAGE_WIDTH - OFFSET, 50, this.text1));
    }

    if (this.text2 != null && this.text2.length > 0) {
      list.add(new TextElement(0, y + OFFSET, BookScreen.PAGE_WIDTH, BookScreen.PAGE_HEIGHT - OFFSET - y, this.text2));
    }
  }

  @Override
  public HtmlSerializable toHTML(BookData book) {
    HtmlGroup group = HtmlGroup.indent().add(makeTitleHTML());

    if (image != null) {
      HtmlElement box = HtmlElement.div().classes("column")
        .style("margin-left", 2 * OFFSET)
        .style("height", 2 * 50);

      group.add(box);

      if (text1 != null) box.add(TextData.toHtml(text1, book));
      if (text2 != null) group.add(HtmlElement.div().classes("column").add(TextData.toHtml(text2, book)));
    } else {
      if (text1 != null) group.add(TextData.toHtml(text1, book));
      if (text2 != null) group.add(TextData.toHtml(text2, book));
    }

    return group;
  }
}
