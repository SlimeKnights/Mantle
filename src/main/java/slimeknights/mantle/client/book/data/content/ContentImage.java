package slimeknights.mantle.client.book.data.content;

import java.util.ArrayList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import slimeknights.mantle.client.book.data.element.ImageData;
import slimeknights.mantle.client.gui.book.GuiBook;
import slimeknights.mantle.client.gui.book.element.BookElement;
import slimeknights.mantle.client.gui.book.element.ElementImage;

@SideOnly(Side.CLIENT)
public class ContentImage extends PageContent {

  public ImageData image;

  @Override
  public void build(ArrayList<BookElement> list) {
    if (image != null && image.location != null)
      list.add(new ElementImage(15, 15, GuiBook.PAGE_WIDTH - 30, GuiBook.PAGE_HEIGHT - 30, image));
    else
      list.add(new ElementImage(image));
  }
}
