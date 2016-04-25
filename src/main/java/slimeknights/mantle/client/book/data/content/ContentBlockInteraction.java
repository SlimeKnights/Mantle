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

public class ContentBlockInteraction extends PageContent {

  public static final transient int TEX_SIZE = 512;
  public static final transient ImageData IMG_SMITHING = new ImageData(TEX_MISC, 0, 0, 88, 55, TEX_SIZE, TEX_SIZE);

  public static final transient int INPUT_X = 6;
  public static final transient int INPUT_Y = 18;
  public static final transient int BLOCK_X = 40;
  public static final transient int BLOCK_Y = 26;

  public static final transient float ITEM_SCALE = 2.0F;
  public static final transient float BLOCK_SCALE = 5.0F;

  public String title = "Block Interaction";
  public ItemStackData input;
  public ItemStackData block;
  public TextData[] description;

  @Override
  public void build(BookData book, ArrayList<BookElement> list, boolean rightSide) {
    int x = GuiBook.PAGE_WIDTH / 2 - IMG_SMITHING.width / 2 - 10;
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

    if(block != null && !block.id.equals("")) {
      list.add(new ElementItem(x + BLOCK_X, y + BLOCK_Y, BLOCK_SCALE, block.getItems(), block.action));
    }

    if(description != null && description.length > 0) {
      list.add(new ElementText(0, IMG_SMITHING.height + y + 50, GuiBook.PAGE_WIDTH, GuiBook.PAGE_HEIGHT - IMG_SMITHING.height - y - 50, description));
    }
  }
}
