package slimeknights.mantle.client.book;

import slimeknights.mantle.Mantle;
import slimeknights.mantle.client.book.data.BookData;

import javax.annotation.Nullable;

/**
 * Should be implemented by classes that extend {@link slimeknights.mantle.client.book.data.content.PageContent}
 * and any {@link slimeknights.mantle.client.screen.book.element}'s contained within said class that contains
 * any text that would normally be render in the book page.
 */
public interface IHTML {

  /**
   * Converts content to HTML
   * returns null for content that contains no text
   * @param book reference to the parent BookData
   */
  @Nullable
  default String toHTML(BookData book) {
    Mantle.logger.warn("{} does not implement IHTML.", this.getClass());
    return "<p>" + this.getClass() + "</p>";
  }
}
