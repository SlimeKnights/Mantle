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

public class ContentTextRightImage extends PageContent {
  public static final ResourceLocation ID = Mantle.getResource("text_right_image");

  @Getter
  public String title;
  public ImageData image;

  /** Text placed on the left of the image */
  public TextData[] left_text;
  /** Text placed below the image */
  public TextData[] bottom_text;

  private final int OFFSET = 55;

  @Override
  public void build(BookData book, ArrayList<BookElement> list, boolean rightSide) {
    int y = getTitleHeight();

    if (this.title == null || this.title.isEmpty()) {
      y = 0;
    } else {
      this.addTitle(list, this.title);
    }

    if (this.left_text != null && this.left_text.length > 0) {
      list.add(new TextElement(0, y, BookScreen.PAGE_WIDTH - OFFSET, 50, this.left_text));
    }

    if (this.image != null && this.image.location != null) {
      list.add(new ImageElement(BookScreen.PAGE_WIDTH - 50, y, 50, 50, this.image));
    } else {
      list.add(new ImageElement(BookScreen.PAGE_WIDTH - 50, y, 50, 50, ImageData.MISSING));
    }

    if (this.bottom_text != null && this.bottom_text.length > 0) {
      list.add(new TextElement(0, y + OFFSET, BookScreen.PAGE_WIDTH, BookScreen.PAGE_HEIGHT - OFFSET - y, this.bottom_text));
    }
  }

  @Override
  public HtmlSerializable toHTML(BookData book) {
    HtmlGroup group = HtmlGroup.indent().add(makeTitleHTML());

    if (image != null) {
      HtmlElement box = HtmlElement.div().classes("column")
        .style("margin-right", 2 * OFFSET)
        .style("height", 2 * 50);

      group.add(box);

      if (left_text != null) box.add(TextData.toHtml(left_text, book));
      if (bottom_text != null) group.add(HtmlElement.div().classes("column").add(TextData.toHtml(bottom_text, book)));
    } else {
      if (left_text != null) group.add(TextData.toHtml(left_text, book));
      if (bottom_text != null) group.add(TextData.toHtml(bottom_text, book));
    }

    return group;
  }
}
