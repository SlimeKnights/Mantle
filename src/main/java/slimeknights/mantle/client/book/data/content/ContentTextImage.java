package slimeknights.mantle.client.book.data.content;

import java.util.ArrayList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
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
  public void build(ArrayList<BookElement> list) {
    if (text != null && text.length > 0)
      list.add(new ElementText(15, 15, GuiBook.PAGE_WIDTH - 30, GuiBook.PAGE_HEIGHT - 95, text));

    if (image != null && image.location != null)
      list.add(new ElementImage(15, 110, GuiBook.PAGE_WIDTH - 30, 70, image));
    else
      list.add(new ElementImage(ImageData.MISSING));
  }
}
