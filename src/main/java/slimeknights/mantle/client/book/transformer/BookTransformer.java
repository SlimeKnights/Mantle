package slimeknights.mantle.client.book.transformer;

import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.data.content.ContentPadding.PaddingBookTransformer;

/** Base logic for transformers which add pages to books dynamically */
@SuppressWarnings("unused")
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
}
