package slimeknights.mantle.client.book.data.content;

import java.util.ArrayList;

import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.data.SectionData;
import slimeknights.mantle.client.gui.book.GuiBook;
import slimeknights.mantle.client.gui.book.element.BookElement;
import slimeknights.mantle.client.gui.book.element.ElementSection;

public class ContentSectionList extends PageContent {

  protected ArrayList<SectionData> sections = new ArrayList<>();

  public boolean addSection(SectionData data) {
    return this.sections.size() < 9 && this.sections.add(data);
  }

  @Override
  public void build(BookData book, ArrayList<BookElement> list, boolean rightSide) {
    int width = (ElementSection.WIDTH + 5) * 3 - 5;
    int height = (ElementSection.HEIGHT + 5) * 3 - 5;

    int ox = (GuiBook.PAGE_WIDTH - width) / 2;
    int oy = (GuiBook.PAGE_HEIGHT - height) / 2;

    for(int i = 0; i < this.sections.size(); i++) {
      int ix = i % 3;
      int iy = (int) Math.floor(i / 3F);

      int x = ox + ix * (ElementSection.WIDTH + 5);
      int y = oy + iy * (ElementSection.HEIGHT + 5);

      list.add(new ElementSection(x, y, this.sections.get(i)));
    }
  }
}
