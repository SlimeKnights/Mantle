package slimeknights.mantle.client.book.data.content;

import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.client.book.transformer.BookTransformer;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.data.PageData;
import slimeknights.mantle.client.book.data.SectionData;

import java.util.Iterator;

/**
 * Variant of blank pages that only adds the page on a specific side, useful to force the next page to the left or right regardless of the number of pages before
 */
@Getter
public abstract class ContentPadding extends ContentBlank {
  public static final ResourceLocation LEFT_ID = Mantle.getResource("left_padding");
  public static final ResourceLocation RIGHT_ID = Mantle.getResource("right_padding");

  /** If true, this page is padding the left side, false pads the right side */
  public abstract boolean isLeft();

  /** Left variant */
  public static class ContentLeftPadding extends ContentPadding {
    @Override
    public boolean isLeft() {
      return true;
    }
  }

  /** Right variant */
  public static class ContentRightPadding extends ContentPadding {
    @Override
    public boolean isLeft() {
      return false;
    }
  }

  /** Transformer to make this page type work */
  public static class PaddingBookTransformer extends BookTransformer {
    /** Regular transformer considering indexes */
    public static final PaddingBookTransformer INSTANCE = new PaddingBookTransformer();

    private PaddingBookTransformer() {}

    @Override
    public void transform(BookData bookData) {
      // first page is on the right side
      boolean previousLeft = true;

      // iterate through all pages, tracking whehter we are left or right
      for (SectionData section : bookData.sections) {
        Iterator<PageData> pageIterator = section.pages.iterator();
        while (pageIterator.hasNext()) {
          PageData data = pageIterator.next();
          // if its left and the current page is odd, or its right and the current page is even, skip
          if (data.content instanceof ContentPadding && ((ContentPadding) data.content).isLeft() == previousLeft) {
            pageIterator.remove();
          } else {
            previousLeft = !previousLeft;
          }
        }
      }
    }
  }
}
