package slimeknights.mantle.client.book.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

  public String name;
  public ImageData icon;
  public CriteriaData[] unlockCriteria;
  public String data;

  public int pageCount;

  public transient List<PageData> pages = new ArrayList<PageData>();

  @Override
  public int cascadeLoad() {
    name = name.toLowerCase();

    IResource pagesInfo = getResource(getResourceLocation(data));
    if (pagesInfo != null) {
      String data = resourceToString(pagesInfo);
      if (!data.isEmpty())
        pages = Arrays.asList(BookLoader.GSON.fromJson(data, PageData[].class));
    }

    for (PageData page : pages) {
      page.cascadeLoad();
    }

    icon.location = getResourceLocation(icon.file);

    pageCount = pages.size();

    return pageCount;
  }
}
