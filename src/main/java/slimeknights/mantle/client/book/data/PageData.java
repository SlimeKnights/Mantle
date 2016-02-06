package slimeknights.mantle.client.book.data;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
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
          try {
            content = BookLoader.GSON.fromJson(data, BookLoader.getPageType(type));
          } catch (Exception e) {
            content = new ContentError("Failed to create a page of type \"" + type + "\", perhaps the page file \"" + this.data + "\" is missing or invalid?", e);
          }
      }
    }

    if (content == null) {
      try {
        content = BookLoader.getPageType(type).newInstance();
      } catch (InstantiationException | IllegalAccessException e) {
        content = new ContentError("Failed to create a page of type \"" + type + "\", perhaps the type is not registered?");
      }
    }

    for (Field f : content.getClass().getFields()) {
      if (f.getType().isAssignableFrom(ImageData.class) && !Modifier.isTransient(f.getModifiers()))
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
