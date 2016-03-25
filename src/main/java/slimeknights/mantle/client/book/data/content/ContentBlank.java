package slimeknights.mantle.client.book.data.content;

import java.util.ArrayList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.gui.book.element.BookElement;

@SideOnly(Side.CLIENT)
public class ContentBlank extends PageContent {

  @Override
  public void build(BookData book, ArrayList<BookElement> list) {
  }
}
