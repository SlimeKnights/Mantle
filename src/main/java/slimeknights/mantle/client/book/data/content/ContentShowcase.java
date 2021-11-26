package slimeknights.mantle.client.book.data.content;

import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.data.element.ItemStackData;
import slimeknights.mantle.client.book.data.element.TextData;
import slimeknights.mantle.client.screen.book.BookScreen;
import slimeknights.mantle.client.screen.book.element.BookElement;
import slimeknights.mantle.client.screen.book.element.ItemElement;
import slimeknights.mantle.client.screen.book.element.TextElement;

import java.util.ArrayList;

/** Page that showcases an item with text below */
public class ContentShowcase extends PageContent {
  public static final transient String ID = "showcase";

  /** Title of the page */
  public String title = null;
  /** Text to display below the item */
  public TextData[] text;
  /** Item to display */
  public ItemStackData item;

  @Override
  public void build(BookData book, ArrayList<BookElement> list, boolean rightSide) {
    int y = getTitleHeight();

    if (this.title == null || this.title.isEmpty()) {
      y = 0;
    } else {
      this.addTitle(list, this.title);
    }

    if (this.item != null && (!this.item.id.isEmpty() || !this.item.itemList.isEmpty())) {
      list.add(new ItemElement(BookScreen.PAGE_WIDTH / 2 - 15, 15, 2.5f, this.item.getItems(), this.item.action));
    }

    if (this.text != null && this.text.length > 0) {
      list.add(new TextElement(0, y + 20, BookScreen.PAGE_WIDTH, BookScreen.PAGE_HEIGHT - 20 - y, this.text));
    }
  }
}
