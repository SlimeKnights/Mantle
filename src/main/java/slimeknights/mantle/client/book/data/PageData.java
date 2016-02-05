package slimeknights.mantle.client.book.data;

import java.lang.reflect.Field;
import net.minecraft.client.resources.IResource;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import slimeknights.mantle.client.book.BookLoader;
import slimeknights.mantle.client.book.data.content.ContentError;
import slimeknights.mantle.client.book.data.content.PageContent;
import slimeknights.mantle.client.book.data.element.ImageData;
import static slimeknights.mantle.client.book.ResourceHelper.getResource;
import static slimeknights.mantle.client.book.ResourceHelper.getResourceLocation;
import static slimeknights.mantle.client.book.ResourceHelper.resourceToString;

@SideOnly(Side.CLIENT)
public class PageData implements IDataItem {

  public String name = null;
  public String type = "";
  public String data = "";

  public transient SectionData parent;
  public transient PageContent content;

  public PageData() {
    this(false);
  }

  public PageData(boolean custom) {
    if (custom)
      data = "no-load";
  }

  @Override
  public void load() {
    if (name == null)
      name = "page" + parent.unnamedPageCounter++;

    name = name.toLowerCase();

    if (!data.equals("no-load")) {
      IResource pageInfo = getResource(getResourceLocation(data));
      if (pageInfo != null) {
        String data = resourceToString(pageInfo);
        if (!data.isEmpty())
          content = BookLoader.GSON.fromJson(data, BookLoader.getPageType(type));
      }
    }

    if (content == null)
      try {
        content = BookLoader.getPageType(type) != null ? BookLoader.getPageType(type).newInstance() : new ContentError();
      } catch (InstantiationException | IllegalAccessException e) {
        e.printStackTrace();
      }

    for (Field f : content.getClass().getFields()) {
      if (f.getType().isAssignableFrom(ImageData.class))
        try {
          f.setAccessible(true);
          ImageData d = (ImageData) f.get(content);
          d.location = getResourceLocation(d.file, true);
        } catch (IllegalAccessException e) {
          e.printStackTrace();
        }
    }
  }
}
