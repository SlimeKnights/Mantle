package slimeknights.mantle.client.book.data;

import java.util.ArrayList;
import java.util.Arrays;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import slimeknights.mantle.client.book.BookLoader;
import slimeknights.mantle.client.book.BookTransformer;
import slimeknights.mantle.client.gui.book.GuiBook;
import static slimeknights.mantle.client.book.ResourceHelper.getResource;
import static slimeknights.mantle.client.book.ResourceHelper.getResourceLocation;
import static slimeknights.mantle.client.book.ResourceHelper.resourceExists;
import static slimeknights.mantle.client.book.ResourceHelper.resourceToString;

@SideOnly(Side.CLIENT)
public class BookData implements IDataItem {

  public transient ArrayList<SectionData> sections = new ArrayList<>();
  public transient AppearanceData appearance = new AppearanceData();
  public transient int pageCount;
  public transient int fullPageCount;

  public final transient String bookLocation;

  protected final transient ArrayList<BookTransformer> transformers = new ArrayList<>();

  public BookData(String bookLocation) {
    this.bookLocation = bookLocation;
  }

  @Override
  public int cascadeLoad() {
    int pages = 0;

    sections = new ArrayList<>(Arrays.asList(BookLoader.GSON.fromJson(resourceToString(getResource(getResourceLocation("index.json"))), SectionData[].class)));

    ResourceLocation coverLocation = getResourceLocation("appearance.json");

    if (resourceExists(coverLocation))
      appearance = BookLoader.GSON.fromJson(resourceToString(getResource(coverLocation)), AppearanceData.class);
    else
      appearance = new AppearanceData();

    appearance.cascadeLoad();

    for (BookTransformer transformer : transformers)
      transformer.transform(this);

    for (SectionData section : sections) {
      section.parent = this;
      pages += section.cascadeLoad();
    }

    return pages;
  }

  public SectionData findSection(String name) {
    for (SectionData section : sections)
      if (section.name.equals(name.toLowerCase()))
        return section;

    return null;
  }

  public int getFirstPageNumber(SectionData section) {
    int pages = 0;
    for (SectionData sect : sections) {
      if (section == sect)
        return pages + 1;

      pages += sect.pageCount;
    }

    return -1;
  }

  public PageData findPage(int number) {
    if (number < 0)
      return null;

    int pages = 0;
    for (SectionData section : sections) {
      if (pages + section.pageCount > number)
        return section.pages.get(number - pages);
      else
        pages += section.pageCount;
    }

    return null;
  }

  public PageData findPage(String location) {
    return findPage(findPageNumber(location));
  }

  public int findPageNumber(String location) {
    location = location.toLowerCase();

    int pages = 0;

    if (!location.contains("."))
      return -1;

    String sectionName = location.substring(0, location.indexOf('.'));
    String pageName = location.substring(location.indexOf('.') + 1);

    for (SectionData section : sections) {
      if (!sectionName.equals(section.name)) {
        pages += section.pageCount;
        continue;
      }

      for (PageData page : section.pages) {
        if (!pageName.equals(page.name)) {
          pages++;
          continue;
        }

        return pages + 1;
      }
    }

    return -1;
  }

  public void openGui(@Nullable ItemStack item) {
    Minecraft.getMinecraft().displayGuiScreen(new GuiBook(this, item));
  }

  public void addTransformer(BookTransformer transformer) {
    if (!transformers.contains(transformer))
      transformers.add(transformer);
  }
}
