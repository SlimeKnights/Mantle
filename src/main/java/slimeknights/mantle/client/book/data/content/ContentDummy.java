package slimeknights.mantle.client.book.data.content;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.screen.book.element.BookElement;

import java.util.ArrayList;

@OnlyIn(Dist.CLIENT)
public class ContentDummy extends PageContent {

  @Override
  public void build(BookData book, ArrayList<BookElement> list, boolean rightSide) {
    //TODO load from JSON
  }
}
