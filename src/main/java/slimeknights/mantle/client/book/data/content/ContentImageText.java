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
public class ContentImageText extends PageContent {

  public ImageData image;
  public TextData[] text;

  @Override
  public void build(ArrayList<BookElement> list) {
    if (image != null && image.location != null)
      list.add(new ElementImage(0, 0, GuiBook.PAGE_WIDTH, 100, image));
    else
      list.add(new ElementImage(0, 0, 32, 32, ImageData.MISSING));

    if (text != null && text.length > 0)
      list.add(new ElementText(0, 105, GuiBook.PAGE_WIDTH, GuiBook.PAGE_HEIGHT - 105, text));
  }
}
