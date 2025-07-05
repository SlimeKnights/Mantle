package slimeknights.mantle.client.book.data.content;

import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.client.book.HTMLUtils;
import slimeknights.mantle.client.book.IHTML;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.data.element.ImageData;
import slimeknights.mantle.client.book.data.element.TextData;
import slimeknights.mantle.client.screen.book.BookScreen;
import slimeknights.mantle.client.screen.book.element.BookElement;
import slimeknights.mantle.client.screen.book.element.ImageElement;
import slimeknights.mantle.client.screen.book.element.TextElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ContentImageText extends PageContent implements IHTML {
  public static final ResourceLocation ID = Mantle.getResource("image_text");

  @Getter
  public String title = null;
  public ImageData image;
  public TextData[] text;
  public boolean centerImage = true;

  @Override
  public void build(BookData book, ArrayList<BookElement> list, boolean rightSide) {
    int y = getTitleHeight();

    if (this.title == null || this.title.isEmpty()) {
      y = 0;
    } else {
      this.addTitle(list, this.title);
    }

    if (this.image != null && this.image.location != null) {
      int x = 0;
      int width = BookScreen.PAGE_WIDTH;
      if (centerImage && this.image.width != -1) {
        x = (BookScreen.PAGE_WIDTH - this.image.width) / 2;
        width = this.image.width;
      }
      ImageElement element = new ImageElement(x, y, width, 100, this.image);
      list.add(element);
      y += element.height + 5;
    } else {
      list.add(new ImageElement(0, y, 32, 32, ImageData.MISSING));
      y += 37;
    }

    if (this.text != null && this.text.length > 0) {
      list.add(new TextElement(0, y, BookScreen.PAGE_WIDTH, BookScreen.PAGE_HEIGHT - y, this.text));
    }
  }

  public String toHTML() {
    return String.format(
      """
      %s
      <div style="padding-top: %dpx">
          %s
      </div>
      """,
      HTMLUtils.line(title, true),
      image.height * 2,
      TextData.toHTML(text)
    );
  }
}
