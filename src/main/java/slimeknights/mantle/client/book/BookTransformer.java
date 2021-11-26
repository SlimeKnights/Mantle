package slimeknights.mantle.client.book;

import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.data.PageData;
import slimeknights.mantle.client.book.data.SectionData;
import slimeknights.mantle.client.book.data.content.ContentBlank;
import slimeknights.mantle.client.book.data.content.ContentPadding.PaddingBookTransformer;
import slimeknights.mantle.client.book.data.content.ContentSectionList;
import slimeknights.mantle.client.book.data.content.ContentTableOfContents;
import slimeknights.mantle.client.book.data.element.TextData;
import slimeknights.mantle.client.screen.book.BookScreen;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public abstract class BookTransformer {
  /** Adds a transformer which builds a visual index */
  public static BookTransformer indexTranformer() {
    return IndexTranformer.INSTANCE;
  }

  /** Adds a transformer which builds a table of contents */
  public static BookTransformer contentTableTransformer() {
    return ContentTableTransformer.INSTANCE;
  }

  /** Adds a transformer which builds a table of contents for a specific section name */
  @SuppressWarnings("unused") // API
  public static BookTransformer contentTableTransformerForSection(String sectionName) {
    return new ContentTableTransformer(sectionName);
  }

  /** Adds a transformer which removes padding pages if unneeded, should be added to the book last */
  public static BookTransformer paddingTransformer() {
    return PaddingBookTransformer.INSTANCE;
  }

  /**
   * Called when all the sections within the book are loaded.
   *
   * @param book The object to the book to be transformed
   */
  public abstract void transform(BookData book);

  protected static class IndexTranformer extends BookTransformer {

    public static final IndexTranformer INSTANCE = new IndexTranformer();

    /** Gets the pages needed for the given section count, essentially a ceiling divide by 9 */
    private static int getIndexPagesNeeded(int sectionCount) {
      int needed = sectionCount / 9;
      if (sectionCount % 9 != 0) needed++;
      return needed;
    }

    @Override
    public void transform(BookData book) {
      SectionData index = new SectionData(true) {
        @Override
        public void update(@Nullable BookScreen.AdvancementCache advancementCache) {
          this.pages.clear();

          // find how many other sections to draw
          List<SectionData> visibleSections = this.parent.getVisibleSections(advancementCache);
          if (visibleSections.isEmpty()) {
            return;
          }
          visibleSections.remove(0);
          PageData[] pages = new PageData[getIndexPagesNeeded(visibleSections.size())];
          for (int i = 0; i < pages.length; i++) {
            pages[i] = new PageData(true);

            pages[i].name = "page" + (i + 1);

            ContentSectionList content = new ContentSectionList();
            pages[i].content = content;

            for (int j = i * 9; j - i * 9 < 9 && j < visibleSections.size(); j++) {
              content.addSection(visibleSections.get(j));
            }
          }

          this.pages.addAll(Arrays.asList(pages));
        }
      };
      // add in some blank pages so the padding transformer has an accurate count
      List<SectionData> visibleSections = book.getVisibleSections(null);
      if (!visibleSections.isEmpty()) {
        PageData[] pages = new PageData[getIndexPagesNeeded(visibleSections.size() - 1)];
        for (int i = 0; i < pages.length; i++) {
          pages[i] = new PageData(true);
          pages[i].name = "page" + (i + 1);
          pages[i].content = new ContentBlank();
        }
        index.pages.addAll(Arrays.asList(pages));
      }

      index.name = "index";
      book.sections.add(0, index);
    }
  }

  protected static class ContentTableTransformer extends BookTransformer {

    public static final ContentTableTransformer INSTANCE = new ContentTableTransformer();

    private final String sectionToTransform;

    public ContentTableTransformer(String sectionToTransform) {
      this.sectionToTransform = sectionToTransform;
    }

    public ContentTableTransformer() {
      this.sectionToTransform = null;
    }

    @Override
    public void transform(BookData book) {
      final int ENTRIES_PER_PAGE = 24;

      for (SectionData section : book.sections) {
        if (section.name.equals("index")) {
          continue;
        }
        if (this.sectionToTransform != null && !section.name.equals(this.sectionToTransform)) {
          continue;
        }

        int genPages = (int) Math.ceil(section.getPageCount() * 1.F / ENTRIES_PER_PAGE);

        if (genPages == 0) {
          continue;
        }

        PageData[] pages = new PageData[genPages];

        for (int i = 0; i < pages.length; i++) {
          pages[i] = new PageData(true);
          pages[i].name = "tableofcontents" + i;
          TextData[] text = new TextData[i > pages.length - 1 ? ENTRIES_PER_PAGE : section.getPageCount() - (genPages - 1) * ENTRIES_PER_PAGE];

          for (int j = 0; j < text.length; j++) {
            text[j] = new TextData((i * ENTRIES_PER_PAGE + j + 1) + ". " + section.pages.get(i * ENTRIES_PER_PAGE + j).getTitle());
            text[j].action = "go-to-page-rtn:" + section.name + "." + section.pages.get(i * ENTRIES_PER_PAGE + j).name;
          }

          pages[i].content = new ContentTableOfContents(i == 0 ? section.getTitle() : "", text);
        }

        for (int i = pages.length - 1; i >= 0; i--) {
          section.pages.add(0, pages[i]);
        }
      }
    }
  }
}
