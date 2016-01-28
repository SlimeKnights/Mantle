package slimeknights.mantle.client.book.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import slimeknights.mantle.client.book.BookLoader;
import static slimeknights.mantle.client.book.ResourceHelper.getResource;
import static slimeknights.mantle.client.book.ResourceHelper.getResourceLocation;
import static slimeknights.mantle.client.book.ResourceHelper.resourceExists;
import static slimeknights.mantle.client.book.ResourceHelper.resourceToString;

@SideOnly(Side.CLIENT)
public class BookData implements IDataItem {

  public transient List<SectionData> sections = new ArrayList<>();
  public transient AppearanceData appearance = new AppearanceData();
  public transient int pageCount;
  public transient int fullPageCount;

  public final transient String bookLocation;

  public BookData(String bookLocation) {
    this.bookLocation = bookLocation;
  }

  @Override
  public int cascadeLoad() {
    int pages = 0;

    sections = Arrays.asList(BookLoader.GSON.fromJson(resourceToString(getResource(getResourceLocation("index.json"))), SectionData[].class));

    ResourceLocation coverLocation = getResourceLocation("appearance.json");

    if (resourceExists(coverLocation))
      appearance = BookLoader.GSON.fromJson(resourceToString(getResource(coverLocation)), AppearanceData.class);
    else
      appearance = new AppearanceData();

    appearance.cascadeLoad();

    for (SectionData section : sections) {
      pages += section.cascadeLoad();
    }

    appearance.cascadeLoad();

    return pages;
  }

  public PageData findPage(int number) {
    int pages = 0;
    for (SectionData section : sections) {
      if (pages + section.pageCount > number)
        return section.pages.get(number - pages);
      else
        pages += section.pageCount;
    }

    return null;
  }

  public int findPageNumber(String location){
    location = location.toLowerCase();

    int pages = 0;

    if(!location.contains("."))
      return -1;

    String sectionName = location.substring(0, location.indexOf('.'));
    String pageName = location.substring(location.indexOf('.') + 1);

    for (SectionData section : sections) {
      if(!sectionName.equals(section.name)) {
        pages += section.pageCount;
        continue;
      }

      for (PageData page : section.pages){
        if(!pageName.equals(page.name)){
          pages++;
          continue;
        }

        return pages + 1;
      }
    }

    return -1;
  }
}
