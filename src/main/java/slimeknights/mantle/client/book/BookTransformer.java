package slimeknights.mantle.client.book;

import java.util.ArrayList;
import java.util.Arrays;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.data.PageData;
import slimeknights.mantle.client.book.data.SectionData;
import slimeknights.mantle.client.book.data.content.ContentSectionList;

public abstract class BookTransformer {

  /**
   * Called when all the sections within the book are loaded, but before their contents are, as the pages are counted when the contents of the sections are loaded.
   *
   * @param book The object to the book to transform
   */
  public abstract void transform(BookData book);

  protected static class IndexTranformer extends BookTransformer {

    @Override
    public void transform(BookData book) {
      SectionData index = new SectionData(true);
      index.name = "index";

      PageData[] pages = new PageData[(int) Math.ceil(book.sections.size() / 9F)];

      for (int i = 0; i < pages.length; i++) {
        pages[i] = new PageData(true);

        pages[i].name = "page" + (i + 1);

        ContentSectionList content = new ContentSectionList();
        pages[i].content = content;

        for (int j = i * 9; j - i * 9 < 9 && j < book.sections.size(); j++) {
          content.addSection(book.sections.get(j));
        }
      }

      index.pages = new ArrayList<>(Arrays.asList(pages));
      book.sections.add(0, index);
    }
  }
}
