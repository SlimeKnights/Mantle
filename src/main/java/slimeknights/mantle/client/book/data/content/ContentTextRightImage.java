package slimeknights.mantle.client.book.data.content;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;

import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.data.element.ImageData;
import slimeknights.mantle.client.book.data.element.TextData;
import slimeknights.mantle.client.gui.book.GuiBook;
import slimeknights.mantle.client.gui.book.element.BookElement;
import slimeknights.mantle.client.gui.book.element.ElementImage;
import slimeknights.mantle.client.gui.book.element.ElementText;

@SideOnly(Side.CLIENT)
public class ContentTextRightImage extends PageContent {

  public String title;
  public TextData[] text1;
  public TextData[] text2;
  public ImageData image;

  @Override
  public void build(BookData book, ArrayList<BookElement> list) {
    int y = TITLE_HEIGHT;

    if(title == null || title.isEmpty()) {
      y = 0;
    } else {
      addTitle(list, title);
    }

    if(text1 != null && text1.length > 0) {
      list.add(new ElementText(0, y, GuiBook.PAGE_WIDTH - 55, 50, text1));
    }

    if(image != null && image.location != null) {
      list.add(new ElementImage(GuiBook.PAGE_WIDTH - 50, y, 50, 50, image));
    } else {
      list.add(new ElementImage(GuiBook.PAGE_WIDTH - 50, y, 50, 50, ImageData.MISSING));
    }

    if(text2 != null && text2.length > 0) {
      list.add(new ElementText(0, y + 55, GuiBook.PAGE_WIDTH, GuiBook.PAGE_HEIGHT - 55 - y, text2));
    }
  }
}
