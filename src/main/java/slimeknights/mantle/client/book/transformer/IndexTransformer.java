package slimeknights.mantle.client.book.transformer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.data.PageData;
import slimeknights.mantle.client.book.data.SectionData;
import slimeknights.mantle.client.book.data.content.ContentBlank;
import slimeknights.mantle.client.book.data.content.ContentSectionList;
import slimeknights.mantle.client.screen.book.BookScreen;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class IndexTransformer extends BookTransformer {
  public static final IndexTransformer INSTANCE = new IndexTransformer();
  public static final ResourceLocation INDEX_EXTRA_DATA = Mantle.getResource("index");

  private static final Set<ResourceLocation> hiddenPageTypes = new HashSet<>();

  /** Divides a number by the given divisor, rounding up */
  private static int ceilingDivide(int value, int divisor) {
    int result = value / divisor;
    if (value % divisor != 0) result++;
    return result;
  }

  private List<SectionData> getVisibleSections(@Nullable BookScreen.AdvancementCache advancementCache, BookData book) {
    return filterHiddenSections(book.getVisibleSections(advancementCache));
  }

  @Override
  public void transform(BookData book) {
    int sectionsPerPage = book.appearance.drawFourColumnIndex ? 12 : 9;
    SectionData index = new SectionData(true) {
      @Override
      public void update(@Nullable BookScreen.AdvancementCache advancementCache) {
        this.pages.clear();

        // find how many other sections to draw
        List<SectionData> visibleSections = getVisibleSections(advancementCache, book);
        if (visibleSections.isEmpty()) {
          return;
        }
        visibleSections.remove(0);
        PageData[] pages = new PageData[ceilingDivide(visibleSections.size(), sectionsPerPage)];
        for (int i = 0; i < pages.length; i++) {
          pages[i] = new PageData(true);

          pages[i].name = "page" + (i + 1);

          ContentSectionList content = new ContentSectionList();
          pages[i].content = content;

          int pageStart = i * sectionsPerPage;
          for (int j = pageStart; j - pageStart < 16 && j < visibleSections.size(); j++) {
            content.addSection(visibleSections.get(j));
          }
        }

        this.pages.addAll(Arrays.asList(pages));
      }
    };
    // add in some blank pages so the padding transformer has an accurate count
    List<SectionData> visibleSections = getVisibleSections(null, book);
    if (!visibleSections.isEmpty()) {
      PageData[] pages = new PageData[ceilingDivide(visibleSections.size() - 1, sectionsPerPage)];
      for (int i = 0; i < pages.length; i++) {
        pages[i] = new PageData(true);
        pages[i].name = "page" + (i + 1);
        pages[i].content = new ContentBlank();
      }
      index.pages.addAll(Arrays.asList(pages));
    }

    index.name = "index";
    book.sections.add(0, index);
  }

  /**
   * Tests whether a given page is hidden for the purposes of index transformers
   * @param page The page to test
   * @return whether the page is hidden
   */
  public static boolean isPageHidden(PageData page) {
    if(hiddenPageTypes.contains(page.type)) {
      return true;
    }

    if (page.extraData.containsKey(INDEX_EXTRA_DATA)) {
      JsonElement data = page.extraData.get(INDEX_EXTRA_DATA);
      if (data.isJsonObject()) {
        JsonObject dataObject = data.getAsJsonObject();
        if (dataObject.has("hidden")) {
          JsonElement hidden = dataObject.get("hidden");
          if (hidden.isJsonPrimitive() && hidden.getAsJsonPrimitive().isBoolean()) {
            return hidden.getAsBoolean();
          }
        }
      }
    }

    return false;
  }

  /**
   * Tests whether a given section is hidden for the purposes of index transformers
   * @param section The section to test
   * @return whether the section is hidden
   */
  public static boolean isSectionHidden(SectionData section) {
    if(section.extraData.containsKey(INDEX_EXTRA_DATA)) {
      JsonElement data = section.extraData.get(INDEX_EXTRA_DATA);
      if (data.isJsonObject()) {
        JsonObject dataObject = data.getAsJsonObject();
        if (dataObject.has("hidden")) {
          JsonElement hidden = dataObject.get("hidden");
          if (hidden.isJsonPrimitive() && hidden.getAsJsonPrimitive().isBoolean()) {
            return hidden.getAsBoolean();
          }
        }
      }
    }

    return false;
  }

  /**
   * Filters the given pages, removing any sections that are marked to be hidden for index purposes
   * @param pages The pages to filter
   * @return A list that only contains non-hidden pages
   */
  public static List<PageData> filterHiddenPages(List<PageData> pages) {
    return pages.stream().filter(page -> !isPageHidden(page)).collect(Collectors.toList());
  }

  /**
   * Filters the given sections, removing any sections that are marked to be hidden for index purposes
   * @param sections The sections to filter
   * @return A list that only contains non-hidden sections
   */
  public static List<SectionData> filterHiddenSections(List<SectionData> sections) {
    return sections.stream().filter(section -> !isSectionHidden(section)).collect(Collectors.toList());
  }

  /**
   * Adds a page type to be implicitly hidden from all indexes
   * @apiNote This will implicitly hide this page type from all books, not just your own, so caution is advised
   * @param pageType The type of page to implicitly hide
   */
  public static void addHiddenPageType(ResourceLocation pageType) {
    hiddenPageTypes.add(pageType);
  }
}
