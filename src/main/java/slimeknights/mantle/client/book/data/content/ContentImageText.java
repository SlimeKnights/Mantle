package slimeknights.mantle.client.book.data.content;

import java.util.ArrayList;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.data.element.ImageData;
import slimeknights.mantle.client.book.data.element.TextData;
import slimeknights.mantle.client.gui.book.GuiBook;
import slimeknights.mantle.client.gui.book.element.BookElement;
import slimeknights.mantle.client.gui.book.element.ElementImage;
import slimeknights.mantle.client.gui.book.element.ElementText;

@OnlyIn(Dist.CLIENT)
public class ContentImageText extends PageContent {

  public String title = null;
  public ImageData image;
  public TextData[] text;

  @Override
  public void build(BookData book, ArrayList<BookElement> list, boolean rightSide) {
    int y = TITLE_HEIGHT;

    if(title == null || title.isEmpty()) {
      y = 0;
    } else {
      addTitle(list, title);
    }

    if(image != null && image.location != null) {
      list.add(new ElementImage(0, y, GuiBook.PAGE_WIDTH, 100, image));
    } else {
      list.add(new ElementImage(0, y, 32, 32, ImageData.MISSING));
    }

    if(text != null && text.length > 0) {
      list.add(new ElementText(0, y + 105, GuiBook.PAGE_WIDTH, GuiBook.PAGE_HEIGHT - 105 - y, text));
    }
  }
}
