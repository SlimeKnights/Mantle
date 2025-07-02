package slimeknights.mantle.client.book;

public interface IHTML {
  /** Convert content to HTML */
  default String toHTML() {
    return "<p>" + this.getClass() + "</p>";
  }
}
