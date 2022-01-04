package slimeknights.mantle.client.book.data.content;

import lombok.Getter;
import lombok.Setter;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.data.PageData;
import slimeknights.mantle.client.book.data.element.TextData;
import slimeknights.mantle.client.book.repository.BookRepository;
import slimeknights.mantle.client.screen.book.BookScreen;
import slimeknights.mantle.client.screen.book.element.BookElement;
import slimeknights.mantle.client.screen.book.element.TextElement;

import javax.annotation.Nullable;
import java.util.ArrayList;

/** Base for all page content */
public abstract class PageContent {

  public static final transient int TITLE_HEIGHT = 16;
  public static final transient int LARGE_TITLE_HEIGHT = 20;

  public transient PageData parent;
  public transient BookRepository source;

  /** If true, the title will be centered */
  @Setter @Getter @Nullable
  private Boolean centerTitle;
  /** If true, the title will be large */
  @Setter @Getter @Nullable
  private Boolean largeTitle;

  /** Returns the title for this page content, for the sake of indexes */
  @Nullable
  public String getTitle() {
    return "";
  }

  /**
   * Call when the GUI is opened to initialize content
   */
  public void load() {}

  /**
   * Called once to initialize book content
   */
  public abstract void build(BookData book, ArrayList<BookElement> list, boolean rightSide);

  /** Returns true if the title should be large */
  private boolean isLarge() {
    if (largeTitle != null) {
      return largeTitle;
    }
    else if (parent != null && parent.parent != null && parent.parent.parent != null) {
      return parent.parent.parent.appearance.largePageTitles;
    }
    return false;
  }

  /** Returns true if the title should be centered */
  private boolean isCentered() {
    if (centerTitle != null) {
      return centerTitle;
    }
    else if (parent != null && parent.parent != null && parent.parent.parent != null) {
      return parent.parent.parent.appearance.centerPageTitles;
    }
    return false;
  }

  /**
   * Gets the title height for the given page
   */
  protected int getTitleHeight() {
    return isLarge() ? LARGE_TITLE_HEIGHT : TITLE_HEIGHT;
  }

  /**
   * Adds a title to the book with default properties
   * @param list       List of book elements
   * @param titleText  Title text
   */
  public void addTitle(ArrayList<BookElement> list, String titleText) {
    this.addTitle(list, titleText, false);
  }

  /**
   * Adds a title to the book with an optional shadow
   * @param list       List of book elements
   * @param titleText  Title text
   * @param dropShadow If true, adds a shadow
   */
  public void addTitle(ArrayList<BookElement> list, String titleText, boolean dropShadow) {
    this.addTitle(list, titleText, dropShadow, 0, 0);
  }

  /**
   * Adds a title to the book with the given color and shadow
   * @param list       List of book elements
   * @param titleText  Title text
   * @param dropShadow If true, adds a shadow
   * @param color      Color hex code in RRGGBB format
   */
  public void addTitle(ArrayList<BookElement> list, String titleText, boolean dropShadow, int color) {
    this.addTitle(list, titleText, dropShadow, color, 0);
  }

  /**
   * Adds a title to the book with full options
   * @param list       List of book elements
   * @param titleText  Title text
   * @param dropShadow If true, adds a shadow
   * @param color      Color hex code in RRGGBB format
   * @param y          Starting Y position of the title
   */
  public void addTitle(ArrayList<BookElement> list, String titleText, boolean dropShadow, int color, int y) {
    TextData title = new TextData(titleText);

    boolean isLarge = isLarge();
    title.scale = isLarge ? 1.2f : 1.0f;
    title.underlined = true;
    title.dropshadow = dropShadow;

    if (color != 0) {
      title.useOldColor = false;
      title.rgbColor = color;
    }

    int x = 0;
    int w = BookScreen.PAGE_WIDTH;
    if (isCentered()) {
      w = (int)Math.ceil(this.parent.parent.parent.fontRenderer.width(titleText) * title.scale) + 1;
      x = (BookScreen.PAGE_WIDTH - w) / 2;
    }
    list.add(new TextElement(x, y, w, isLarge ? 11 : 9, title));
  }

  /** Adds text to the book at the top */
  public void addText(ArrayList<BookElement> list, String subText, boolean dropShadow) {
    this.addText(list, subText, dropShadow, 0, 0);
  }

  /** Adds text to the book at the top with the given color */
  public void addText(ArrayList<BookElement> list, String subText, boolean dropShadow, int color) {
    this.addText(list, subText, dropShadow, color, 0);
  }

  /**
   * Adds a text to the book at the given locaiton
   * @param list       List of book elements
   * @param text       Text
   * @param dropShadow If true, adds a shadow
   * @param color      Color hex code in RRGGBB format
   * @param y          Starting Y position of the title
   * @return Height in pixels of the added text
   */
  public int addText(ArrayList<BookElement> list, String text, boolean dropShadow, int color, int y) {
    TextData subText = new TextData(text);
    subText.dropshadow = dropShadow;
    if (color != 0) {
      subText.useOldColor = false;
      subText.rgbColor = color;
    }
    int height = this.parent.parent.parent.fontRenderer.wordWrapHeight(text, BookScreen.PAGE_WIDTH) * 12 / 9;
    list.add(new TextElement(5, y, BookScreen.PAGE_WIDTH, height, subText));
    return height;
  }
}
