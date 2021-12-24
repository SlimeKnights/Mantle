package slimeknights.mantle.client.book.transformer;

import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.data.PageData;
import slimeknights.mantle.client.book.data.SectionData;
import slimeknights.mantle.client.book.data.content.ContentPadding.PaddingBookTransformer;
import slimeknights.mantle.client.book.data.content.ContentTableOfContents;
import slimeknights.mantle.client.book.data.element.TextData;

import java.util.List;

public abstract class BookTransformer {
  /** Adds a transformer which builds a visual index */
  public static BookTransformer indexTranformer() {
    return IndexTransformer.INSTANCE;
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

        List<PageData> sectionPages = IndexTransformer.filterHiddenPages(section.pages);

        int genPages = (int) Math.ceil(sectionPages.size() * 1.F / ENTRIES_PER_PAGE);

        if (genPages == 0) {
          continue;
        }

        PageData[] pages = new PageData[genPages];

        for (int i = 0; i < pages.length; i++) {
          pages[i] = new PageData(true);
          pages[i].name = "tableofcontents" + i;
          TextData[] text = new TextData[i > pages.length - 1 ? ENTRIES_PER_PAGE : sectionPages.size() - (genPages - 1) * ENTRIES_PER_PAGE];

          for (int j = 0; j < text.length; j++) {
            text[j] = new TextData((i * ENTRIES_PER_PAGE + j + 1) + ". " + sectionPages.get(i * ENTRIES_PER_PAGE + j).getTitle());
            text[j].action = "mantle:go-to-page-rtn " + section.name + "." + sectionPages.get(i * ENTRIES_PER_PAGE + j).name;
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
