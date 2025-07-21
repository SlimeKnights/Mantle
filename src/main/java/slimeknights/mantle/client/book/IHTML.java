package slimeknights.mantle.client.book;

import slimeknights.mantle.Mantle;

import javax.annotation.Nullable;

public interface IHTML {
  /** Convert content to HTML */
  @Nullable
  default String toHTML() {
    Mantle.logger.warn("{} does not implement IHTML.", this.getClass());
    return "<p>" + this.getClass() + "</p>";
  }
}
