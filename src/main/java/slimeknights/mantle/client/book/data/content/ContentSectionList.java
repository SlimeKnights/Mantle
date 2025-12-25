package slimeknights.mantle.client.book.data.content;

import slimeknights.mantle.client.book.HTMLUtils;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.data.SectionData;
import slimeknights.mantle.client.screen.book.BookScreen;
import slimeknights.mantle.client.screen.book.element.BookElement;
import slimeknights.mantle.client.screen.book.element.SelectionElement;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class ContentSectionList extends PageContent {

  protected ArrayList<SectionData> sections = new ArrayList<>();

  public boolean addSection(SectionData data) {
    return this.sections.size() < 12 && this.sections.add(data);
  }

  @Override
  public void build(BookData book, ArrayList<BookElement> list, boolean rightSide) {
    int columns = book.appearance.drawFourColumnIndex ? 4 : 3;
    int width = (SelectionElement.WIDTH + 5) * columns - 5;
    int height = (SelectionElement.HEIGHT + 5) * 3 - 5;

    int ox = (BookScreen.PAGE_WIDTH - width) / 2;
    int oy = (BookScreen.PAGE_HEIGHT - height) / 2 - 5;

    int sectionRange = Math.min(columns * 3, this.sections.size());
    for (int i = 0; i < sectionRange; i++) {
      int ix = i % columns;
      int iy = i / columns;

      int x = ox + ix * (SelectionElement.WIDTH + 5);
      int y = oy + iy * (SelectionElement.HEIGHT + 5);

      list.add(new SelectionElement(x, y, this.sections.get(i)));
    }
  }

  @Override
  public String toHTML() {
    return "<div class=\"grid-content-" + (sections.size() >= 9 ? 4 : 3) + " mc-font-gray\">\n" +
      sections.stream()
        .map(section -> {
          int page_number = section.parent.getFirstPageNumber(section, null);
          return String.format(
            """
            <div data-minetip-title="%s">
            <a href="../page-%d/#%s.%s"><img src="/assets/images/book/icons/blank.png" alt=""></a>
            %s
            </div>
            """,
            section.getTitle(),
            page_number / 2,
            section.name,
            // why -1
            section.parent.findPage(page_number - 1, null).name,
            HTMLUtils.p(section.getTitle())
          );
        })
        .collect(Collectors.joining()) +
      "</div>";
  }
}
