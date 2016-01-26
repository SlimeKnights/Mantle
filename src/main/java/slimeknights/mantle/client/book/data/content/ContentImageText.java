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
public class ContentImageText extends PageContent {

  public ImageData image;
  public TextData[] text;

  @Override
  public void build(ArrayList<BookElement> list) {
    if(image != null && image.location != null)
      list.add(new ElementImage(15, 15, GuiBook.PAGE_WIDTH - 30, 70, image));
    else
      list.add(new ElementImage(15, 15, 32, 32, ImageData.MISSING));

    if(text != null && text.length > 0)
      list.add(new ElementText(15, 90, GuiBook.PAGE_WIDTH - 30, GuiBook.PAGE_HEIGHT - 90, text));
  }
}
