package slimeknights.mantle.client.book.data;

public class BookmarkData {

  public String text = "";
  public int color = 0x464646; // Index bookmark : 0x1ED400
  public String page = "";

  public BookmarkData() {
    this("");
  }

  public BookmarkData(String page) {
    this(page, "");
  }

  public BookmarkData(String page, String text) {
    this.page = page;
    this.text = text;
  }
}
