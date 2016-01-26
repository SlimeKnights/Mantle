package slimeknights.mantle.client.book.data.content;

import java.util.ArrayList;
import slimeknights.mantle.client.book.data.element.ImageData;
import slimeknights.mantle.client.book.data.element.TextData;
import slimeknights.mantle.client.gui.book.GuiBook;
import slimeknights.mantle.client.gui.book.element.BookElement;
import slimeknights.mantle.client.gui.book.element.ElementImage;
import slimeknights.mantle.client.gui.book.element.ElementText;

/**
 * @author fuj1n
 */
public class ContentTextRightImage extends PageContent {

  public TextData[] text1;
  public TextData[] text2;
  public ImageData image;

  @Override
  public void build(ArrayList<BookElement> list) {
    if(text1 != null && text1.length > 0)
      list.add(new ElementText(15, 15, GuiBook.PAGE_WIDTH - 82, 50, text1));

    if(image != null && image.location != null)
      list.add(new ElementImage(15 + GuiBook.PAGE_WIDTH - 85, 15, 50, 50, image));
    else
      list.add(new ElementImage(15 + GuiBook.PAGE_WIDTH - 85, 15, 50, 50, ImageData.MISSING));

    if(text2 != null && text2.length > 0)
      list.add(new ElementText(15, 70, GuiBook.PAGE_WIDTH - 30, GuiBook.PAGE_HEIGHT - 70, text2));
  }
}
