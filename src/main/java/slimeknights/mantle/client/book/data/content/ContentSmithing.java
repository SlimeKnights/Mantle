package slimeknights.mantle.client.book.data.content;

import java.util.ArrayList;

import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.data.element.ImageData;
import slimeknights.mantle.client.book.data.element.ItemStackData;
import slimeknights.mantle.client.book.data.element.TextData;
import slimeknights.mantle.client.gui.book.GuiBook;
import slimeknights.mantle.client.gui.book.element.BookElement;
import slimeknights.mantle.client.gui.book.element.ElementImage;
import slimeknights.mantle.client.gui.book.element.ElementItem;
import slimeknights.mantle.client.gui.book.element.ElementText;

import static slimeknights.mantle.client.gui.book.Textures.TEX_MISC;

public class ContentSmithing extends PageContent {

  public static final transient int TEX_SIZE = 512;
  public static final transient ImageData IMG_SMITHING = new ImageData(TEX_MISC, 88, 0, 210, 42, TEX_SIZE, TEX_SIZE);

  public static final transient int INPUT_X = 5;
  public static final transient int INPUT_Y = 5;
  public static final transient int MODIFIER_X = 89;
  public static final transient int MODIFIER_Y = 5;
  public static final transient int RESULT_X = 173;
  public static final transient int RESULT_Y = 5;

  public static final transient float ITEM_SCALE = 2.0F;

  public String title = "Smithing";
  public ItemStackData input;
  public ItemStackData modifier;
  public ItemStackData result;
  public TextData[] description;

  @Override
  public void build(BookData book, ArrayList<BookElement> list, boolean rightSide) {
    int x = GuiBook.PAGE_WIDTH / 2 - IMG_SMITHING.width / 2;
    int y = TITLE_HEIGHT;

    if(title == null || title.isEmpty()) {
      y = 0;
    } else {
      addTitle(list, title);
    }

    list.add(new ElementImage(x, y, IMG_SMITHING.width, IMG_SMITHING.height, IMG_SMITHING, book.appearance.coverColor));

    if(input != null && !input.id.equals("")) {
      list.add(new ElementItem(x + INPUT_X, y + INPUT_Y, ITEM_SCALE, input.getItems(), input.action));
    }

    if(modifier != null && !modifier.id.equals("")) {
      list.add(new ElementItem(x + MODIFIER_X, y + MODIFIER_Y, ITEM_SCALE, modifier.getItems(), modifier.action));
    }

    if(result != null && !result.id.equals("")) {
      list.add(new ElementItem(x + RESULT_X, y + RESULT_Y, ITEM_SCALE, result.getItems(), result.action));
    }

    if(description != null && description.length > 0) {
      list.add(new ElementText(0, IMG_SMITHING.height + y + 5, GuiBook.PAGE_WIDTH, GuiBook.PAGE_HEIGHT - IMG_SMITHING.height - y - 5, description));
    }
  }
}
