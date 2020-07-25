package slimeknights.mantle.client.book;

import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.data.PageData;
import slimeknights.mantle.client.book.data.SectionData;
import slimeknights.mantle.client.book.data.content.ContentSectionList;
import slimeknights.mantle.client.book.data.content.ContentTableOfContents;
import slimeknights.mantle.client.book.data.element.TextData;
import slimeknights.mantle.client.screen.book.BookScreen;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class BookTransformer {

  public static BookTransformer indexTranformer() {
    return IndexTranformer.INSTANCE;
  }

  public static BookTransformer contentTableTransformer() {
    return ContentTableTransformer.INSTANCE;
  }

  @SuppressWarnings("unused") // API
  public static BookTransformer contentTableTransformerForSection(String sectionName) {
    return new ContentTableTransformer(sectionName);
  }

  /**
   * Called when all the sections within the book are loaded.
   *
   * @param book The object to the book to be transformed
   */
  public abstract void transform(BookData book);

  protected static class IndexTranformer extends BookTransformer {

    public static final IndexTranformer INSTANCE = new IndexTranformer();

    @Override
    public void transform(BookData book) {
      SectionData index = new SectionData(true) {
        @Override
        public void update(@Nullable BookScreen.AdvancementCache advancementCache) {
          this.pages.clear();

          List<SectionData> visibleSections = this.parent.getVisibleSections(advancementCache);

          if (visibleSections.isEmpty()) {
            return;
          }

          visibleSections.remove(0);

          PageData[] pages = new PageData[(int) Math.ceil(visibleSections.size() / 9F)];

          for (int i = 0; i < pages.length; i++) {
            pages[i] = new PageData(true);

            pages[i].name = "page" + (i + 1);

            ContentSectionList content = new ContentSectionList();
            pages[i].content = content;

            for (int j = i * 9; j - i * 9 < 9 && j < visibleSections.size(); j++) {
              content.addSection(visibleSections.get(j));
            }
          }

          this.pages = new ArrayList<>(Arrays.asList(pages));
        }
      };

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
          TextData[] text = new TextData[i > pages.length - 1 ? ENTRIES_PER_PAGE : section
                  .getPageCount() - (genPages - 1) * ENTRIES_PER_PAGE];
          for (int j = 0; j < text.length; j++) {
            text[j] = new TextData((i * ENTRIES_PER_PAGE + j + 1) + ". " + section.pages.get(i * ENTRIES_PER_PAGE + j)
                    .getTitle());
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
