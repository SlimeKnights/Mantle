package slimeknights.mantle.client.book.data;

import java.util.ArrayList;
import java.util.Arrays;
import net.minecraft.client.resources.IResource;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import slimeknights.mantle.client.book.BookLoader;
import slimeknights.mantle.client.book.data.element.CriteriaData;
import slimeknights.mantle.client.book.data.element.ImageData;
import static slimeknights.mantle.client.book.ResourceHelper.getResource;
import static slimeknights.mantle.client.book.ResourceHelper.getResourceLocation;
import static slimeknights.mantle.client.book.ResourceHelper.resourceToString;

@SideOnly(Side.CLIENT)
public class SectionData implements IDataItem {

  public String name = null;
  public String title = "";
  public ImageData icon = new ImageData();
  public CriteriaData[] unlockCriteria = new CriteriaData[0];
  public String data = "";

  public transient int unnamedPageCounter = 0;
  public transient BookData parent;
  public transient ArrayList<PageData> pages = new ArrayList<>();

  public SectionData() {
    this(false);
  }

  public SectionData(boolean custom) {
    if (custom)
      data = "no-load";
  }

  @Override
  public void load() {
    if (name == null)
      name = "section" + parent.unnamedSectionCounter++;

    name = name.toLowerCase();

    if (!data.equals("no-load")) {
      IResource pagesInfo = getResource(getResourceLocation(data));
      if (pagesInfo != null) {
        String data = resourceToString(pagesInfo);
        if (!data.isEmpty())
          pages = new ArrayList<>(Arrays.asList(BookLoader.GSON.fromJson(data, PageData[].class)));
      }
    }

    for (PageData page : pages) {
      page.parent = this;
      page.load();
    }

    icon.location = getResourceLocation(icon.file, true);
  }

  public int getPageCount() {
    return pages.size();
  }
}
