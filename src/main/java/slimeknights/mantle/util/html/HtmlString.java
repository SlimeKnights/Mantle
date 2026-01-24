package slimeknights.mantle.util.html;

/** Contains a literal string in HTML text */
public record HtmlString(String value) implements HtmlSerializable {
  @Override
  public void toHtml(StringBuilder builder, String indent) {
    builder.append(escapeString(value));
  }

  @Override
  public String toString() {
    return '"' + value + '"';
  }

  /** Escapes the given string for writing in HTML */
  public static String escapeString(String text) {
    return text.replace("&", "&amp;")
      .replace("<", "&lt;")
      .replace(">", "&gt;")
      .replace("\"", "&quot;");
  }
}
