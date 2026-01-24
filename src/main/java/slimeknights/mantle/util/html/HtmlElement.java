package slimeknights.mantle.util.html;

import slimeknights.mantle.data.loadable.common.ColorLoadable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** Represents an opening and closing Html tag containing some contents. */
public class HtmlElement extends HtmlGroup {
  /** Tags that are self-closing and thus cannot contain children. */
  private static final Set<String> SELF_CLOSING = Set.of("area", "base", "br", "col", "embed", "hr", "img", "input", "link", "meta", "param", "source", "track", "wbr");

  /** Tag type, such as div or ul */
  private final String tag;
  /** List of classes to add to the element */
  private final List<String> classes = new ArrayList<>();
  /** Map of attributes to add to the element. Should not include class or style */
  private final Map<String,String> attributes = new LinkedHashMap<>();
  /** Map of style attributes to add to the element. */
  private final Map<String,String> style = new LinkedHashMap<>();
  /** Cache of whether this is a self closing tag */
  private final boolean selfClosing;

  private HtmlElement(String tag, boolean indentChildren) {
    super(indentChildren);
    this.tag = tag;
    this.selfClosing = SELF_CLOSING.contains(tag);
  }

  /** Creates a tag that indents children */
  public static HtmlElement indent(String tag) {
    return new HtmlElement(tag, true);
  }

  /** Creates a tag that does not indent children */
  public static HtmlElement inline(String tag) {
    return new HtmlElement(tag, false);
  }

  @Override
  public HtmlGroup add(HtmlSerializable element) {
    if (selfClosing) {
      throw new IllegalStateException("Cannot add children to self-closing elements");
    }
    return super.add(element);
  }


  /* Attributes */

  /** Adds the passed classes */
  public HtmlElement classes(String... classes) {
    Collections.addAll(this.classes, classes);
    return this;
  }

  /** Adds an attribute to the element. */
  public HtmlElement attribute(String name, String value) {
    if ("style".equals(name)) {
      throw new IllegalArgumentException("Use HtmlElement#style()");
    }
    // just redirect class to the class list
    if ("class".equals(name)) {
      return classes(value);
    }
    attributes.put(name, value);
    return this;
  }

  /** Sets the element ID */
  public HtmlElement id(String id) {
    return attribute("id", id);
  }

  /** Sets the element tooltip using the minecraft style. */
  public HtmlElement minetip(String text) {
    return attribute("data-minetip-title", text);
  }


  /* Style */

  /** Adds style to the element */
  public HtmlElement style(String name, String value) {
    style.put(name, value);
    return this;
  }

  /** Adds style to the element in terms of pixels */
  public HtmlElement style(String name, int value) {
    return style(name, value + "px");
  }

  /** Adds style to the element in terms of pixels */
  public HtmlElement style(String name, float value) {
    return style(name, Math.round(value * 100) + "%");
  }

  /** Adds a color element to the style */
  public HtmlElement color(String name, int color) {
    return style("color", '#' + ColorLoadable.NO_ALPHA.getString(color));
  }

  /** Adds the color element to the style */
  public HtmlElement color(int color) {
    return color("color", color);
  }

  /** Sets the element to be bold */
  public HtmlElement bold() {
    return style("font-weight", "bold");
  }

  /** Sets the element to be italic */
  public HtmlElement italic() {
    return style("font-weight", "italic");
  }

  /** Underlines the element */
  public HtmlElement underline() {
    return style("text-decoration", "underline");
  }

  /** Underlines the element */
  public HtmlElement strikeout() {
    return style("text-decoration", "strikeout");
  }


  @Override
  public void toHtml(StringBuilder builder, String indent) {
    // start '<tag'
    builder.append('<').append(tag);

    // do classes first
    if (!classes.isEmpty()) {
      builder.append(" class=\"");
      builder.append(String.join(" ", classes));
      builder.append('"');
    }

    // add attributes, format 'name="value"'
    for (Map.Entry<String,String> entry : attributes.entrySet()) {
      builder.append(" ")
        .append(entry.getKey())
        .append("=\"")
        .append(HtmlString.escapeString(entry.getValue()))
        .append('"');
    }
    // add style if set, format 'style="..."'
    if (!style.isEmpty()) {
      builder.append(" style=\"");
      // each style element format 'key: value; '
      builder.append(style.entrySet().stream()
        .map(entry -> entry.getKey() + ": " + entry.getValue() + ';')
        .collect(Collectors.joining(" ")));
      builder.append('"');
    }

    // for self-closing tags, use HTML5 style
    if (selfClosing) {
      builder.append("/>");
    // for non-self closing tags, write the full closing tag immediately if no children
    } else if (children.isEmpty()) {
      builder.append("></").append(tag).append('>');
    } else {
      // close opening tag
      builder.append('>');
      if (indentChildren) {
        builder.append('\n');
      }

      // add all nested elements, indent if requested
      super.toHtml(builder, indent + "  ");

      // close tag
      if (indentChildren) {
        builder.append(indent);
      }
      builder.append("</").append(tag).append('>');
    }
  }

  @Override
  public String toString() {
    String s = '<' + tag + '>';
    if (!children.isEmpty()) {
      s += '[' + children.stream().map(Object::toString).collect(Collectors.joining(",")) + ']';
    }
    return s;
  }


  /* Common block elements */

  /** Creates a div element */
  public static HtmlElement div() {
    return indent("div");
  }

  /** Creates an unordered list element */
  public static HtmlElement ul() {
    return indent("ul");
  }

  /** Creates a list item */
  public static HtmlElement li() {
    return indent("li");
  }


  /* Common inline elements */

  /** Creates a paragraph element */
  public static HtmlElement p() {
    return inline("p");
  }

  /** Creates a span element */
  public static HtmlElement span() {
    return inline("span");
  }

  /** Creates a bold element */
  public static HtmlElement b() {
    return inline("b");
  }

  /** Creates an italic element */
  public static HtmlElement i() {
    return inline("i");
  }

  /** Creates a line break element. Different return type ensures no children or attributes are added by accident. */
  public static HtmlSerializable br() {
    return inline("br");
  }
}
