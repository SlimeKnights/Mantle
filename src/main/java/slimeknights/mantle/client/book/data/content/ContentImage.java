package slimeknights.mantle.client.book.data.content;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;

import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.data.element.ImageData;
import slimeknights.mantle.client.gui.book.GuiBook;
import slimeknights.mantle.client.gui.book.element.BookElement;
import slimeknights.mantle.client.gui.book.element.ElementImage;

@SideOnly(Side.CLIENT)
public class ContentImage extends PageContent {

  public String title = null;
  public ImageData image;

  @Override
  public void build(BookData book, ArrayList<BookElement> list) {
    int y = TITLE_HEIGHT;

    if(title == null || title.isEmpty()) {
      y = 0;
    } else {
      addTitle(list, title);
    }

    if(image != null && image.location != null) {
      list.add(new ElementImage(0, y, GuiBook.PAGE_WIDTH, GuiBook.PAGE_HEIGHT - y, image));
    } else {
      list.add(new ElementImage(ImageData.MISSING));
    }
  }
}
