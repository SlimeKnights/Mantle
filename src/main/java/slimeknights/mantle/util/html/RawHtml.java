package slimeknights.mantle.util.html;

/** Html element for adding raw HTML without the builder. Usage of this class is best avoided. */
public record RawHtml(String contents) implements HtmlSerializable {
  @Override
  public void toHtml(StringBuilder builder, String indent) {
    builder.append(contents);
  }

  @Override
  public String toString() {
    return "Raw{" + contents + '}';
  }
}
