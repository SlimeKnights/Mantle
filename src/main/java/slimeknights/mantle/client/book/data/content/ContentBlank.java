package slimeknights.mantle.client.book.data.content;


import java.util.ArrayList;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.gui.book.element.BookElement;

@OnlyIn(Dist.CLIENT)
public class ContentBlank extends PageContent {

  @Override
  public void build(BookData book, ArrayList<BookElement> list, boolean rightSide) {
  }
}
