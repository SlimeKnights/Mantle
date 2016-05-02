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

import static slimeknights.mantle.client.gui.book.Textures.TEX_CRAFTING;

public class ContentCrafting extends PageContent {

  public static final transient int TEX_SIZE = 256;
  public static final transient ImageData IMG_CRAFTING_LARGE = new ImageData(TEX_CRAFTING, 0, 0, 183, 114, TEX_SIZE, TEX_SIZE);
  public static final transient ImageData IMG_CRAFTING_SMALL = new ImageData(TEX_CRAFTING, 0, 114, 155, 78, TEX_SIZE, TEX_SIZE);

  public static final transient int X_RESULT_SMALL = 118;
  public static final transient int Y_RESULT_SMALL = 23;
  public static final transient int X_RESULT_LARGE = 146;
  public static final transient int Y_RESULT_LARGE = 41;

  public static final transient float ITEM_SCALE = 2.0F;
  public static final transient int SLOT_MARGIN = 5;
  public static final transient int SLOT_PADDING = 4;

  public String title = "Crafting";
  public String grid_size = "large";
  public ItemStackData[][] grid;
  public ItemStackData result;
  public TextData[] description;

  @Override
  public void build(BookData book, ArrayList<BookElement> list, boolean rightSide) {
    int x = 0;
    int y = 16;
    int height = 100;
    int resultX = 100;
    int resultY = 50;

    TextData tdTitle = new TextData(title);
    tdTitle.underlined = true;
    list.add(new ElementText(0, 0, GuiBook.PAGE_WIDTH, 9, new TextData[]{tdTitle}));

    if(grid_size.equalsIgnoreCase("small")) {
      x = GuiBook.PAGE_WIDTH / 2 - IMG_CRAFTING_SMALL.width / 2;
      height = y + IMG_CRAFTING_SMALL.height;
      list.add(new ElementImage(x, y, IMG_CRAFTING_SMALL.width, IMG_CRAFTING_SMALL.height, IMG_CRAFTING_SMALL, book.appearance.slotColor));
      resultX = x + X_RESULT_SMALL;
      resultY = y + Y_RESULT_SMALL;
    } else if(grid_size.equalsIgnoreCase("large")) {
      x = GuiBook.PAGE_WIDTH / 2 - IMG_CRAFTING_LARGE.width / 2;
      height = y + IMG_CRAFTING_LARGE.height;
      list.add(new ElementImage(x, y, IMG_CRAFTING_LARGE.width, IMG_CRAFTING_LARGE.height, IMG_CRAFTING_LARGE, book.appearance.slotColor));
      resultX = x + X_RESULT_LARGE;
      resultY = y + Y_RESULT_LARGE;
    }

    if(grid != null) {
      for(int i = 0; i < grid.length; i++) {
        for(int j = 0; j < grid[i].length; j++) {
          if(grid[i][j].id.equals("")) {
            continue;
          }
          list.add(new ElementItem(x + SLOT_MARGIN + (SLOT_PADDING + Math
              .round(ElementItem.ITEM_SIZE_HARDCODED * ITEM_SCALE)) * j, y + SLOT_MARGIN + (SLOT_PADDING + Math
              .round(ElementItem.ITEM_SIZE_HARDCODED * ITEM_SCALE)) * i, ITEM_SCALE, grid[i][j]
                                       .getItems(), grid[i][j].action));
        }
      }
    }

    if(result != null) {
      list.add(new ElementItem(resultX, resultY, ITEM_SCALE, result.getItems(), result.action));
    }

    if(description != null && description.length > 0) {
      list.add(new ElementText(0, height + 5, GuiBook.PAGE_WIDTH, GuiBook.PAGE_HEIGHT - height - 5, description));
    }
  }
}
