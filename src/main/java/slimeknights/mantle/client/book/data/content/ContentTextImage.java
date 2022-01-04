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

import java.util.ArrayList;

public class ContentTextImage extends PageContent {
  public static final ResourceLocation ID = Mantle.getResource("text_image");

  @Getter
  public String title = null;
  public TextData[] text;
  public ImageData image;

  @Override
  public void build(BookData book, ArrayList<BookElement> list, boolean rightSide) {
    int y = getTitleHeight();

    if (this.title == null || this.title.isEmpty()) {
      y = 0;
    } else {
      this.addTitle(list, this.title);
    }

    if (this.text != null && this.text.length > 0) {
      list.add(new TextElement(0, y, BookScreen.PAGE_WIDTH, BookScreen.PAGE_HEIGHT - 105, this.text));
    }

    if (this.image != null && this.image.location != null) {
      list.add(new ImageElement(0, y + BookScreen.PAGE_HEIGHT - 100, BookScreen.PAGE_WIDTH, 100 - y, this.image));
    } else {
      list.add(new ImageElement(0, y + BookScreen.PAGE_HEIGHT - 100, BookScreen.PAGE_WIDTH, 100 - y, ImageData.MISSING));
    }
  }
}
