package slimeknights.mantle.client.book.data.content;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;

import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.data.element.ImageData;
import slimeknights.mantle.client.book.data.element.TextData;
import slimeknights.mantle.client.gui.book.GuiBook;
import slimeknights.mantle.client.gui.book.element.BookElement;
import slimeknights.mantle.client.gui.book.element.ElementImage;
import slimeknights.mantle.client.gui.book.element.ElementText;

@OnlyIn(Dist.CLIENT)
public class ContentTextImage extends PageContent {

  public String title = null;
  public TextData[] text;
  public ImageData image;

  @Override
  public void build(BookData book, ArrayList<BookElement> list, boolean rightSide) {
    int y = TITLE_HEIGHT;

    if(this.title == null || this.title.isEmpty()) {
      y = 0;
    } else {
      this.addTitle(list, this.title);
    }

    if(this.title == null || this.title.isEmpty()) {
      y = 0;
    } else {
      this.addTitle(list, this.title);
    }

    if(this.text != null && this.text.length > 0) {
      list.add(new ElementText(0, y, GuiBook.PAGE_WIDTH, GuiBook.PAGE_HEIGHT - 105, this.text));
    }

    if(this.image != null && this.image.location != null) {
      list.add(new ElementImage(0, y + GuiBook.PAGE_HEIGHT - 100, GuiBook.PAGE_WIDTH, 100 - y, this.image));
    } else {
      list.add(new ElementImage(0, y + GuiBook.PAGE_HEIGHT - 100, GuiBook.PAGE_WIDTH, 100 - y, ImageData.MISSING));
    }
  }
}
