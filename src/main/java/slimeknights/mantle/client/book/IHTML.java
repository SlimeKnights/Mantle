package slimeknights.mantle.client.book;

import slimeknights.mantle.Mantle;

import javax.annotation.Nullable;

public interface IHTML {
  /** Convert content to HTML */
  @Nullable
  default String toHTML() {
    Mantle.logger.warn("{} does not implement IHTML. If it does not contain text, ignore this warning.", this.getClass());
    return "<p>" + this.getClass() + "</p>";
  }
}
