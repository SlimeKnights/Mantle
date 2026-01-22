package slimeknights.mantle.client.book.data.content;

import com.google.common.collect.Lists;
import lombok.Getter;
import net.minecraft.network.chat.Component;
import slimeknights.mantle.client.book.HTMLUtils;
import slimeknights.mantle.client.book.action.StringActionProcessor;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.data.PageData;
import slimeknights.mantle.client.book.data.SectionData;
import slimeknights.mantle.client.book.data.content.ContentPadding.ContentRightPadding;
import slimeknights.mantle.client.screen.book.BookScreen;
import slimeknights.mantle.client.screen.book.element.BookElement;
import slimeknights.mantle.client.screen.book.element.PageIconLinkElement;
import slimeknights.mantle.client.screen.book.element.SizedBookElement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Index page where each link in the index is an icon rather than text. Used notably for material pages in Tinkers' Construct.
 * Generally created in a custom {@link slimeknights.mantle.client.book.transformer.BookTransformer}.
 */
public class ContentPageIconList extends PageContent {

  protected final int width;
  protected final int height;

  @Getter
  public String title;
  public String subText;
  public float maxScale = 2.5f;
  public static final int xOff = 15;

  protected List<PageIconLinkElement> elements = Lists.newArrayList();

  public ContentPageIconList() {
    this(20);
  }

  public ContentPageIconList(int size) {
    this(size, size);
  }

  public ContentPageIconList(int width, int height) {
    this.width = width;
    this.height = height;
  }

  /**
   * Returns false if the page is full
   */
  public boolean addLink(SizedBookElement element, Component name, PageData pageData) {
    if (this.elements.size() >= this.getMaxIconCount()) {
      return false;
    }
    this.elements.add(new PageIconLinkElement(0, 0, element, name, pageData));
    return true;
  }

  public int getMaxIconCount() {
    return this.getMaxColumns() * this.getMaxRows();
  }

  public int getMaxRows() {
    int totalHeight = BookScreen.PAGE_HEIGHT;
    if (title != null) {
      totalHeight -= getTitleHeight();
    }
    if (subText != null) {
      totalHeight -= 16 + this.parent.parent.parent.fontRenderer.wordWrapHeight(subText, BookScreen.PAGE_WIDTH) * 12 / 9;
    }
    return totalHeight / this.height;
  }

  public int getMaxColumns() {
    return (BookScreen.PAGE_WIDTH - 30) / this.width;
  }

  @Override
  public void build(BookData book, ArrayList<BookElement> list, boolean rightSide) {
    int yOff = 0;
    if (this.title != null) {
      this.addTitle(list, this.title, false);
      yOff = getTitleHeight();
    }

    if(this.subText != null) {
      int height = this.addText(list, this.subText, false, 0, yOff);
      yOff = height + 16;
    }

    int x = xOff;
    int y = yOff;

    float scale = getScale(yOff);
    int scaledWidth = (int) (this.width * scale);
    int scaledHeight = (int) (this.height * scale);

    for (PageIconLinkElement element : this.elements) {
      element.x = x;
      element.y = y;
      element.displayElement.x = x + (int) (scale * (this.width - element.displayElement.width) / 2);
      element.displayElement.y = y + (int) (scale * (this.height - element.displayElement.height) / 2);

      element.width = scaledWidth;
      element.height = scaledHeight;
      element.displayElement.scale(scale);

      list.add(element);

      x += scaledWidth;

      if (x > BookScreen.PAGE_WIDTH - xOff - scaledWidth) {
        x = xOff;
        y += scaledHeight;
        // do not draw over the page
        if (y > BookScreen.PAGE_HEIGHT - scaledHeight) {
          break;
        }
      }
    }
  }

  /** Generates the list of indexes for the given count. */
  public static List<ContentPageIconList> getPagesNeededForItemCount(int count, SectionData data, String title, String subText) {
    List<ContentPageIconList> listPages = new ArrayList<>();
    List<PageData> newPages = new ArrayList<>();
    while (count > 0) {
      ContentPageIconList overview = new ContentPageIconList();
      PageData page = new PageData(true);
      page.source = data.source;
      page.parent = data;
      page.content = overview;
      page.load();

      // wait to add to the page list until after we added the padding page
      newPages.add(page);

      overview.title = title;
      overview.subText = subText;

      listPages.add(overview);

      count -= overview.getMaxIconCount();
    }

    // ensure same size for all
    if (listPages.size() > 1) {
      listPages.forEach(page -> page.maxScale = 1f);
    }

    // add a padding page if we have an even number of index pages, so you see both together
    if (listPages.size() % 2 == 0) {
      PageData padding = new PageData(true);
      padding.source = data.source;
      padding.parent = data;
      padding.content = new ContentRightPadding();
      padding.load();
      // hack: add padding to the previous section so section links start at the index
      int sectionIndex = data.parent.sections.indexOf(data);
      if (sectionIndex > 0) {
        data.parent.sections.get(sectionIndex - 1).pages.add(padding);
      } else {
        newPages.add(0, padding);
      }
    }

    // padding done, can add new pages at the start of the section
    data.pages.addAll(0, newPages);

    return listPages;
  }

  /** Record to help with icon list building */
  public record PageWithIcon(SizedBookElement icon, PageData page) {}

  /**
   * Adds the list of pages with icons to the indexes and the book
   * @param data       Section to receive pages
   * @param indexList  List of indexes from {@link #getPagesNeededForItemCount(int, SectionData, String, String)}
   * @param pages      List of pages to add to the indexes.
   * @param index      Start index to insert new pages. Should generally be the size of {@link #getPagesNeededForItemCount(int, SectionData, String, String)},
   *                   though you may need to compare {@link SectionData#pages} count if not using {@link slimeknights.mantle.client.book.transformer.IndexTransformer}.
   */
  public static void addPages(SectionData data, List<ContentPageIconList> indexList, Collection<PageWithIcon> pages, int index) {
    Iterator<ContentPageIconList> indexes = indexList.iterator();
    ContentPageIconList overview = indexes.next();
    List<PageData> newPages = new ArrayList<>(pages.size());
    for (PageWithIcon page : pages) {
      newPages.add(page.page);
      while (!overview.addLink(page.icon, Component.literal(page.page.getTitle()), page.page)) {
        overview = indexes.next();
      }
    }
    if (index == -1) {
      data.pages.addAll(newPages);
    } else {
      data.pages.addAll(index, newPages);
    }
  }

  /**
   * Adds the list of pages with icons to the indexes and the book
   * @param data       Section to receive pages
   * @param indexList  List of indexes from {@link #getPagesNeededForItemCount(int, SectionData, String, String)}
   * @param pages      List of pages to add to the indexes.
   */
  public static void addPages(SectionData data, List<ContentPageIconList> indexList, Collection<PageWithIcon> pages) {
    addPages(data, indexList, pages, indexList.size());
  }

  /** Calculates the largest possible icon scale that will fit all the contents on the page */
  protected float getScale(int yOff) {
    int pageW = BookScreen.PAGE_WIDTH - 2 * xOff;
    int pageH = BookScreen.PAGE_HEIGHT - yOff;

    float scale = this.maxScale;
    boolean fits = false;
    while (!fits && scale > 1f) {
      scale -= 0.25f;
      int rows = pageW / (int) (this.width * scale);
      int cols = pageH / (int) (this.height * scale);
      fits = rows * cols >= this.elements.size();
    }
    return scale;
  }

  @Override
  public String toHTML(BookData book) {
    int yOff = 0;
    if (this.title != null) yOff = getTitleHeight();
    if (this.subText != null) yOff = book.fontRenderer.wordWrapHeight(this.subText, 182) * 12 / 9 + 16;

    return String.format(
      """
      %s
      %s
      <div class="grid-icon-list grid-icon-list-%d" style="top: %dpx">
      %s
      </div>
      """,
      getTitleHTML(),
      HTMLUtils.p(subText, "padding-left: 10px"),
      (BookScreen.PAGE_WIDTH - 2 * xOff) / (int) (this.width * getScale(yOff)),
      yOff * 2,
      elements.stream().map(e -> e.toHTML(book)).collect(Collectors.joining("\n"))
    );
  }
}
