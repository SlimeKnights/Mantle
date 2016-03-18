package slimeknights.mantle.client.book.data.content;

import java.util.ArrayList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.data.PageData;
import slimeknights.mantle.client.book.data.element.TextData;
import slimeknights.mantle.client.book.repository.BookRepository;
import slimeknights.mantle.client.gui.book.GuiBook;
import slimeknights.mantle.client.gui.book.element.BookElement;
import slimeknights.mantle.client.gui.book.element.ElementText;

@SideOnly(Side.CLIENT)
public abstract class PageContent {

  public static final transient int TITLE_HEIGHT = 16;

  public transient PageData parent;
  public transient BookRepository source;

  public void load() {
  }

  public abstract void build(BookData book, ArrayList<BookElement> list);

  public void addTitle(ArrayList<BookElement> list, String title) {
    TextData tdTitle = new TextData(title);
    tdTitle.underlined = true;
    addTitle(list, new TextData[]{tdTitle});
  }

  public void addTitle(ArrayList<BookElement> list, TextData[] title) {
    list.add(new ElementText(0, 0, GuiBook.PAGE_WIDTH, 9, title));
  }
}
