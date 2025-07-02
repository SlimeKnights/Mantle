package slimeknights.mantle.client.book;

import slimeknights.mantle.client.book.data.element.TextData;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.stream.Collectors;

public class HTMLUtils {
  public static String paragraphs(TextData[] lines) {
    return Arrays.stream(lines)
      .map(TextData::toHTML)
      .collect(Collectors.joining("\n"));
  }

  public static String line(String text) {
    return line(text, false);
  }

  public static String line(String text, boolean title) {
    return line(text, title, null);
  }

  public static String line(String text, @Nullable String styles) {
    return line(text, false, styles);
  }

  public static String line(String text, boolean title, @Nullable String styles) {
    return String.format("<p%s%s>%s</p>", title ? " class=\"title\"" : "", styles != null ? " style=\"" + styles + "\"" : "", text);
  }

  public static String hexRGB(int rgb) {
    return String.format("#%02X%02X%02X", (rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, (rgb & 0xFF));
  }
}
