package slimeknights.mantle.client.book.data.content;

import java.util.ArrayList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import slimeknights.mantle.client.book.data.element.TextData;
import slimeknights.mantle.client.gui.book.GuiBook;
import slimeknights.mantle.client.gui.book.element.BookElement;
import slimeknights.mantle.client.gui.book.element.ElementText;

/**
 * @author fuj1n
 */
@SideOnly(Side.CLIENT)
public class ContentText extends PageContent {
  public TextData[] text;

  @Override
  public void build(ArrayList<BookElement> list) {
    if(text != null && text.length > 0)
      list.add(new ElementText(15, 15, GuiBook.PAGE_WIDTH - 30, GuiBook.PAGE_HEIGHT - 30, text));
  }
}
