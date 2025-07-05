package slimeknights.mantle.client.book;


import javax.annotation.Nullable;

public class HTMLUtils {
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
    return String.format(
          "<p%s%s%s>%s</p>",
          title ? " id=\"" + slugify(text) + "\"" : "",
          title ? " class=\"title\"" : "",
          styles != null ? " style=\"" + styles + "\"" : "",
          text
    );
  }

  public static String hexRGB(int rgb) {
    return String.format("#%02X%02X%02X", (rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, (rgb & 0xFF));
  }

  public static String slugify(String s) {
    return s.toLowerCase().replace(' ', '_');
  }
}
