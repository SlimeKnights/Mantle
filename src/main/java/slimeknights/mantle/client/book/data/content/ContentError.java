package slimeknights.mantle.client.book.data.content;

import java.util.ArrayList;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import slimeknights.mantle.client.book.data.element.TextData;
import slimeknights.mantle.client.gui.book.GuiBook;
import slimeknights.mantle.client.gui.book.element.BookElement;
import slimeknights.mantle.client.gui.book.element.ElementItem;
import slimeknights.mantle.client.gui.book.element.ElementText;

/**
 * @author fuj1n
 */
@SideOnly(Side.CLIENT)
public class ContentError extends PageContent {

  @Override
  public void build(ArrayList<BookElement> list) {
    TextData[] text = new TextData[1];
    text[0] = new TextData("Could not load page, perhaps the file is missing or the page type does not exist?");
    text[0].color = "dark_red";
    text[0].underlined = true;

    list.add(new ElementText(15, 15, GuiBook.PAGE_WIDTH - 30, GuiBook.PAGE_HEIGHT - 30, text));
  }
}
