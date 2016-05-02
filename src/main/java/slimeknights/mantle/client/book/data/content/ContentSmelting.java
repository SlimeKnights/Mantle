package slimeknights.mantle.client.book.data.content;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

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

import static slimeknights.mantle.client.gui.book.Textures.TEX_SMELTING;

public class ContentSmelting extends PageContent {

  public static final transient int TEX_SIZE = 128;
  public static final transient ImageData IMG_SMELTING = new ImageData(TEX_SMELTING, 0, 0, 110, 114, TEX_SIZE, TEX_SIZE);

  public static final transient int INPUT_X = 5;
  public static final transient int INPUT_Y = 5;
  public static final transient int RESULT_X = 74;
  public static final transient int RESULT_Y = 41;
  public static final transient int FUEL_X = 5;
  public static final transient int FUEL_Y = 77;

  public static final transient float ITEM_SCALE = 2.0F;

  public String title = "Smelting";
  public ItemStackData input;
  public ItemStackData result;
  public TextData[] description;

  @Override
  public void build(BookData book, ArrayList<BookElement> list, boolean rightSide) {
    int x = GuiBook.PAGE_WIDTH / 2 - IMG_SMELTING.width / 2;
    int y = TITLE_HEIGHT;

    TextData tdTitle = new TextData(title);
    tdTitle.underlined = true;
    list.add(new ElementText(0, 0, GuiBook.PAGE_WIDTH, 9, new TextData[]{tdTitle}));
    list.add(new ElementImage(x, y, IMG_SMELTING.width, IMG_SMELTING.height, IMG_SMELTING, book.appearance.slotColor));

    if(input != null && !input.id.equals("")) {
      list.add(new ElementItem(x + INPUT_X, y + INPUT_Y, ITEM_SCALE, input.getItems(), input.action));
    }

    if(result != null && !result.id.equals("")) {
      list.add(new ElementItem(x + RESULT_X, y + RESULT_Y, ITEM_SCALE, result.getItems(), result.action));
    }

    list.add(new ElementItem(x + FUEL_X, y + FUEL_Y, ITEM_SCALE, getFuelsList()));

    if(description != null && description.length > 0) {
      list.add(new ElementText(0, IMG_SMELTING.height + y + 5, GuiBook.PAGE_WIDTH, GuiBook.PAGE_HEIGHT - y - 5, description));
    }
  }

  public ItemStack[] getFuelsList() {
    //TODO ask JEI for fuel list if it is present

    ItemStack[] fuels = new ItemStack[13];
    fuels[0] = new ItemStack(Blocks.WOODEN_SLAB);
    fuels[1] = new ItemStack(Blocks.PLANKS);
    fuels[2] = new ItemStack(Blocks.COAL_BLOCK);
    fuels[3] = new ItemStack(Items.WOODEN_PICKAXE);
    fuels[4] = new ItemStack(Items.WOODEN_SWORD);
    fuels[5] = new ItemStack(Items.WOODEN_HOE);
    fuels[6] = new ItemStack(Items.STICK);
    fuels[7] = new ItemStack(Items.COAL);
    fuels[8] = new ItemStack(Items.LAVA_BUCKET);
    fuels[9] = new ItemStack(Blocks.SAPLING);
    fuels[10] = new ItemStack(Items.BLAZE_ROD);
    fuels[11] = new ItemStack(Items.WOODEN_SHOVEL);
    fuels[12] = new ItemStack(Items.WOODEN_AXE);

    return fuels;
  }
}
