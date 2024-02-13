package slimeknights.mantle.client.book.data.content;

import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.data.PageData;
import slimeknights.mantle.client.book.data.SectionData;
import slimeknights.mantle.client.book.data.element.TextData;
import slimeknights.mantle.client.book.transformer.ContentListingSectionTransformer;
import slimeknights.mantle.client.screen.book.BookScreen;
import slimeknights.mantle.client.screen.book.TextDataRenderer;
import slimeknights.mantle.client.screen.book.element.BookElement;
import slimeknights.mantle.client.screen.book.element.ListingLeftElement;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/** Page content for building an index, instantiate either through {@link ContentIndex} or {@link ContentListingSectionTransformer} */
public class ContentListing extends PageContent {
  public static final int LINE_HEIGHT = 10;

  /** Title to display in the listing */
  @Getter
  public String title = null;
  /** Text to display below the title and before the index */
  public String subText = null;

  /** Outer list represents all columns, inner list represents entries in a column */
  private transient final List<List<TextData>> entries = Util.make(() -> {
    List<List<TextData>> lists = new ArrayList<>(1);
    lists.add(new ArrayList<>());
    return lists;
  });

  /**
   * Adds an entry to the list
   * @param text        Title of entry
   * @param link        Page to link to
   * @param subSection  If true, this entry is a subsection and will be bold with no bullet point
   */
  public void addEntry(String text, @Nullable PageData link, boolean subSection) {
    TextData data = new TextData(text);
    data.bold = subSection;
    if (link != null) {
      data.action = "mantle:go-to-page-rtn " + link.parent.name + "." + link.name;
    }
    this.entries.get(this.entries.size() - 1).add(data);
  }

  /**
   * Adds an entry to the list that is not a subsection
   * @param text        Title of entry
   * @param link        Page to link to
   */
  public void addEntry(String text, @Nullable PageData link) {
    addEntry(text, link, false);
  }

  /** Forces a column break */
  public void addColumnBreak() {
    if (!this.entries.get(this.entries.size() - 1).isEmpty()) {
      if (this.entries.size() == 3) {
        Mantle.logger.warn("Too many columns in content listing, you should create a second listing instead");
      }
      this.entries.add(new ArrayList<>());
    }
  }

  /** If true, there are entries in this listing */
  public boolean hasEntries() {
    return this.entries.get(0).size() > 0;
  }

  /** Gets the height for a column in pixels */
  private static int getColumnHeight(int yOff) {
    int columnHeight = BookScreen.PAGE_HEIGHT - yOff - 16;
    if (columnHeight % LINE_HEIGHT != 0) {
      columnHeight -= columnHeight % LINE_HEIGHT;
    }
    return columnHeight;
  }

  /** Gets the number of elements that fits in a column, inefficient so suggest not calling this frequently */
  public int getEntriesInColumn(SectionData sectionData) {
    int yOff = 0;
    if (this.title != null) {
      yOff = 16;
    }
    if (this.subText != null) {
      yOff += sectionData.parent.fontRenderer.wordWrapHeight(this.subText, BookScreen.PAGE_WIDTH) * 12 / 9;
    }
    return getColumnHeight(yOff) / LINE_HEIGHT;
  }

  @Override
  public void build(BookData book, ArrayList<BookElement> list, boolean rightSide) {
    int yOff = 0;
    if (this.title != null) {
      this.addTitle(list, this.title, false);
      yOff = 16;
    }
    if (this.subText != null) {
      int height = this.addText(list, this.subText, false, 0, yOff);
      yOff += height;
    }

    // 16 gives space for the bottom and ensures a round number, yOff ensures the top is not counted
    int columnHeight = getColumnHeight(yOff);

    // determine how wide we can make each column, support up to 3
    int width = BookScreen.PAGE_WIDTH;
    int finalColumns = this.entries.size();
    int entriesPerColumn = columnHeight / LINE_HEIGHT;
    if (finalColumns < 3) {
      for (List<TextData> column : this.entries) {
        int totalEntries = column.size();
        while (totalEntries > entriesPerColumn) {
          finalColumns++;
          if (finalColumns == 3) {
            break;
          }
          totalEntries -= entriesPerColumn;
        }
      }
    }
    if (finalColumns > 3) {
      finalColumns = 3;
    }
    width /= finalColumns;

    int x = 0;
    int y = 0;
    for (List<TextData> column : this.entries) {
      // add each page to the column
      for (TextData data : column) {
        if (y >= columnHeight) {
          x += width;
          y = 0;
        }
        String text = data.text;
        if (text.isEmpty()) {
          y += LINE_HEIGHT;
        } else {
          // if (!data.bold) text = "- " + text;
          // int height = this.parent.parent.parent.fontRenderer.wordWrapHeight(text, width) * LINE_HEIGHT / 9;
          int height;
          if (data.bold) {
            height = TextDataRenderer.getLinesForString(text, ChatFormatting.BOLD.toString(), width, "", parent.parent.parent.fontRenderer) * LINE_HEIGHT;
          } else {
            height = TextDataRenderer.getLinesForString(text, "", width, "- ", parent.parent.parent.fontRenderer) * LINE_HEIGHT;
          }
          list.add(new ListingLeftElement(x, y + yOff, width, height, data.bold, data));
          y += height;
        }
      }
      // reset column
      x += width;
      y = 0;
    }
  }
}
