package slimeknights.mantle.client.book.data.content;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.screen.book.element.BookElement;

import java.util.ArrayList;

@Environment(EnvType.CLIENT)
public class ContentDummy extends PageContent {

  @Override
  public void build(BookData book, ArrayList<BookElement> list, boolean rightSide) {
    //TODO load from JSON
  }
}
