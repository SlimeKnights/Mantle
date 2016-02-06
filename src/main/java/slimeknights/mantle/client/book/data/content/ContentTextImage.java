package slimeknights.mantle.client.book.data.content;

import java.util.ArrayList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.data.element.ImageData;
import slimeknights.mantle.client.book.data.element.TextData;
import slimeknights.mantle.client.gui.book.GuiBook;
import slimeknights.mantle.client.gui.book.element.BookElement;
import slimeknights.mantle.client.gui.book.element.ElementImage;
import slimeknights.mantle.client.gui.book.element.ElementText;

@SideOnly(Side.CLIENT)
public class ContentTextImage extends PageContent {

  public TextData[] text;
  public ImageData image;

  @Override
  public void build(BookData book, ArrayList<BookElement> list) {
    if (text != null && text.length > 0)
      list.add(new ElementText(0, 0, GuiBook.PAGE_WIDTH, GuiBook.PAGE_HEIGHT - 105, text));

    if (image != null && image.location != null)
      list.add(new ElementImage(0, GuiBook.PAGE_HEIGHT - 100, GuiBook.PAGE_WIDTH, 100, image));
    else
      list.add(new ElementImage(0, GuiBook.PAGE_HEIGHT - 100, GuiBook.PAGE_WIDTH, 100, ImageData.MISSING));
  }
}
