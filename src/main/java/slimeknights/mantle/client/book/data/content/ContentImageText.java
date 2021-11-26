package slimeknights.mantle.client.book.data.content;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.data.element.ImageData;
import slimeknights.mantle.client.book.data.element.TextData;
import slimeknights.mantle.client.screen.book.BookScreen;
import slimeknights.mantle.client.screen.book.element.BookElement;
import slimeknights.mantle.client.screen.book.element.ImageElement;
import slimeknights.mantle.client.screen.book.element.TextElement;

import java.util.ArrayList;

@OnlyIn(Dist.CLIENT)
public class ContentImageText extends PageContent {

  public String title = null;
  public ImageData image;
  public TextData[] text;

  @Override
  public void build(BookData book, ArrayList<BookElement> list, boolean rightSide) {
    int y = getTitleHeight();

    if (this.title == null || this.title.isEmpty()) {
      y = 0;
    } else {
      this.addTitle(list, this.title);
    }

    if (this.image != null && this.image.location != null) {
      list.add(new ImageElement(0, y, BookScreen.PAGE_WIDTH, 100, this.image));
    } else {
      list.add(new ImageElement(0, y, 32, 32, ImageData.MISSING));
    }

    if (this.text != null && this.text.length > 0) {
      list.add(new TextElement(0, y + 105, BookScreen.PAGE_WIDTH, BookScreen.PAGE_HEIGHT - 105 - y, this.text));
    }
  }
}
