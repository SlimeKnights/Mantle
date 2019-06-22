package slimeknights.mantle.client.book.data.content;

import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

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

    TextData tdTitle = new TextData(this.title);
    tdTitle.underlined = true;
    list.add(new ElementText(0, 0, GuiBook.PAGE_WIDTH, 9, tdTitle));
    list.add(new ElementImage(x, y, IMG_SMELTING.width, IMG_SMELTING.height, IMG_SMELTING, book.appearance.slotColor));

    if(this.input != null && !this.input.id.equals("")) {
      list.add(new ElementItem(x + INPUT_X, y + INPUT_Y, ITEM_SCALE, this.input.getItems(), this.input.action));
    }

    if(this.result != null && !this.result.id.equals("")) {
      list.add(new ElementItem(x + RESULT_X, y + RESULT_Y, ITEM_SCALE, this.result.getItems(), this.result.action));
    }

    list.add(new ElementItem(x + FUEL_X, y + FUEL_Y, ITEM_SCALE, this.getFuelsList()));

    if(this.description != null && this.description.length > 0) {
      list.add(new ElementText(0, IMG_SMELTING.height + y + 5, GuiBook.PAGE_WIDTH, GuiBook.PAGE_HEIGHT - y - 5, this.description));
    }
  }

  public NonNullList<ItemStack> getFuelsList() {
    //TODO ask JEI for fuel list if it is present

    NonNullList<ItemStack> fuels = NonNullList.withSize(28, ItemStack.EMPTY);
    fuels.set(0, new ItemStack(Blocks.OAK_SLAB));
    fuels.set(1, new ItemStack(Blocks.SPRUCE_SLAB));
    fuels.set(2, new ItemStack(Blocks.BIRCH_SLAB));
    fuels.set(3, new ItemStack(Blocks.JUNGLE_SLAB));
    fuels.set(4, new ItemStack(Blocks.ACACIA_SLAB));
    fuels.set(5, new ItemStack(Blocks.DARK_OAK_SLAB));
    fuels.set(6, new ItemStack(Blocks.OAK_PLANKS));
    fuels.set(7, new ItemStack(Blocks.SPRUCE_PLANKS));
    fuels.set(8, new ItemStack(Blocks.BIRCH_PLANKS));
    fuels.set(9, new ItemStack(Blocks.JUNGLE_PLANKS));
    fuels.set(10, new ItemStack(Blocks.ACACIA_PLANKS));
    fuels.set(11, new ItemStack(Blocks.DARK_OAK_PLANKS));
    fuels.set(12, new ItemStack(Blocks.COAL_BLOCK));
    fuels.set(13, new ItemStack(Items.WOODEN_PICKAXE));
    fuels.set(14, new ItemStack(Items.WOODEN_SWORD));
    fuels.set(15, new ItemStack(Items.WOODEN_HOE));
    fuels.set(16, new ItemStack(Items.STICK));
    fuels.set(17, new ItemStack(Items.COAL));
    fuels.set(18, new ItemStack(Items.LAVA_BUCKET));
    fuels.set(19, new ItemStack(Blocks.OAK_SAPLING));
    fuels.set(20, new ItemStack(Blocks.SPRUCE_SAPLING));
    fuels.set(21, new ItemStack(Blocks.BIRCH_SAPLING));
    fuels.set(22, new ItemStack(Blocks.JUNGLE_SAPLING));
    fuels.set(23, new ItemStack(Blocks.ACACIA_SAPLING));
    fuels.set(24, new ItemStack(Blocks.DARK_OAK_SAPLING));
    fuels.set(25, new ItemStack(Items.BLAZE_ROD));
    fuels.set(26, new ItemStack(Items.WOODEN_SHOVEL));
    fuels.set(27, new ItemStack(Items.WOODEN_AXE));

    return fuels;
  }
}
