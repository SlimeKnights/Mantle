package slimeknights.mantle.client.book.data.content;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;

import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.data.element.TextData;
import slimeknights.mantle.client.gui.book.GuiBook;
import slimeknights.mantle.client.gui.book.element.BookElement;
import slimeknights.mantle.client.gui.book.element.ElementText;

@SideOnly(Side.CLIENT)
public class ContentText extends PageContent {

  public String title = null;
  public TextData[] text;

  @Override
  public void build(BookData book, ArrayList<BookElement> list) {
    int y = TITLE_HEIGHT;

    if(title == null || title.isEmpty()) {
      y = 0;
    } else {
      addTitle(list, title);
    }

    if(text != null && text.length > 0) {
      list.add(new ElementText(0, y, GuiBook.PAGE_WIDTH, GuiBook.PAGE_HEIGHT - y, text));
    }
  }
}
