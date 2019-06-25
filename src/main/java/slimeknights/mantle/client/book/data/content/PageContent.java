package slimeknights.mantle.client.book.data.content;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.data.PageData;
import slimeknights.mantle.client.book.data.element.TextData;
import slimeknights.mantle.client.book.repository.BookRepository;
import slimeknights.mantle.client.screen.book.BookScreen;
import slimeknights.mantle.client.screen.book.element.BookElement;
import slimeknights.mantle.client.screen.book.element.ElementText;

import java.util.ArrayList;

@OnlyIn(Dist.CLIENT)
public abstract class PageContent {

  public static final transient int TITLE_HEIGHT = 16;

  public transient PageData parent;
  public transient BookRepository source;

  public void load() {
  }

  public abstract void build(BookData book, ArrayList<BookElement> list, boolean rightSide);

  public void addTitle(ArrayList<BookElement> list, String title) {
    TextData tdTitle = new TextData(title);
    tdTitle.underlined = true;
    this.addTitle(list, new TextData[] { tdTitle });
  }

  public void addTitle(ArrayList<BookElement> list, TextData[] title) {
    list.add(new ElementText(0, 0, BookScreen.PAGE_WIDTH, 9, title));
  }
}
