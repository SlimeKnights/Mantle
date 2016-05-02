package slimeknights.mantle.client.book.data.content;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;

import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.gui.book.element.BookElement;

@SideOnly(Side.CLIENT)
public class ContentBlank extends PageContent {

  @Override
  public void build(BookData book, ArrayList<BookElement> list, boolean rightSide) {
  }
}
