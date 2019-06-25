package slimeknights.mantle.client.book.data.content;

import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.data.SectionData;
import slimeknights.mantle.client.screen.book.BookScreen;
import slimeknights.mantle.client.screen.book.element.BookElement;
import slimeknights.mantle.client.screen.book.element.ElementSection;

import java.util.ArrayList;

public class ContentSectionList extends PageContent {

  protected ArrayList<SectionData> sections = new ArrayList<>();

  public boolean addSection(SectionData data) {
    return this.sections.size() < 9 && this.sections.add(data);
  }

  @Override
  public void build(BookData book, ArrayList<BookElement> list, boolean rightSide) {
    int width = (ElementSection.WIDTH + 5) * 3 - 5;
    int height = (ElementSection.HEIGHT + 5) * 3 - 5;

    int ox = (BookScreen.PAGE_WIDTH - width) / 2;
    int oy = (BookScreen.PAGE_HEIGHT - height) / 2;

    for (int i = 0; i < this.sections.size(); i++) {
      int ix = i % 3;
      int iy = (int) Math.floor(i / 3F);

      int x = ox + ix * (ElementSection.WIDTH + 5);
      int y = oy + iy * (ElementSection.HEIGHT + 5);

      list.add(new ElementSection(x, y, this.sections.get(i)));
    }
  }
}
