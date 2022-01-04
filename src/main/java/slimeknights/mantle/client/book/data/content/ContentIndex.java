package slimeknights.mantle.client.book.data.content;

import com.google.common.collect.ImmutableSet;
import lombok.RequiredArgsConstructor;
import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.transformer.IndexTransformer;
import slimeknights.mantle.client.screen.book.element.BookElement;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;

/**
 * This content makes up a configurable index page in the book.
 *
 * Configuration:
 * <ul>
 *   <li>{@code hidden} Any elements in this array will be skipped in the index</li>
 *   <li>{@code operations} Applies extra transformations to the list</li>
 * </ul>
 * Operations contain {@code before} to determine when they happen, and an {@code action}:
 * <ul>
 *   <li>{@code add_group}: Adds a group header that does not link a page, name will be set to {@code data}</li>
 *   <li>{@code column_break}: Forces a new column</li>
 *   <li>{@code line_break}: Forces a new line</li>
 * </ul>
 */
@SuppressWarnings("unused")
public class ContentIndex extends ContentListing {
  public static final transient ResourceLocation ID = Mantle.getResource("index");

  private transient boolean loaded = false;
  private String[] hidden;
  private Operation[] operations;

  @Override
  public void build(BookData book, ArrayList<BookElement> list, boolean rightSide) {
    // load as late as possible, to ensure the index is properly filled
    if (!loaded) {
      loaded = true;
      // "hidden" is always hidden, plus whatever we are told to hide
      Set<String> hiddenSet = hidden == null
                              ? ImmutableSet.of("hidden")
                              : ImmutableSet.<String>builder().add("hidden").add(hidden).build();
      // allow performing other operations, adding group headers and breaks
      Operation[] operations = Objects.requireNonNullElse(this.operations, new Operation[0]);
      parent.parent.pages.forEach(page -> {
        // no support for splitting into multiple indexes, if you need two, just create two pages and tell it to hide everything from the other
        if (page != parent && !IndexTransformer.isPageHidden(page) && !hiddenSet.contains(page.name)) {
          // perform extra action if anything happens before this page
          for (Operation operation : operations) {
            if (page.name.equals(operation.before)) {
              switch (operation.action) {
                case "add_group"    -> addEntry(operation.data, null, true);
                case "column_break" -> addColumnBreak();
                case "line_break"   -> addEntry("", null, false);
                default -> Mantle.logger.error("Unknown ContentIndex action " + operation.action);
              }
            }
          }
          // if the page name starts with "group_", will be treated as a header with a bold name and no bullet point
          addEntry(page.getTitle(), page, page.name.startsWith("group_"));
        }
      });
    }
    super.build(book, list, rightSide);
  }

  /** Data class for extra index operations we can perform */
  @SuppressWarnings("ClassCanBeRecord") // messes with GSON
  @RequiredArgsConstructor
  protected static final class Operation {
    private final String before;
    private final String action;
    private final String data;
  }
}
