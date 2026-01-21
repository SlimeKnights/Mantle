package slimeknights.mantle.client.book;

import slimeknights.mantle.Mantle;

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
   */
  @Nullable
  default String toHTML() {
    Mantle.logger.warn("{} does not implement IHTML.", this.getClass());
    return "<p>" + this.getClass() + "</p>";
  }
}
