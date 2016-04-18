package slimeknights.mantle.client.book.data;

import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResource;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

import slimeknights.mantle.client.book.BookLoader;
import slimeknights.mantle.client.book.data.content.ContentError;
import slimeknights.mantle.client.book.data.content.PageContent;
import slimeknights.mantle.client.book.data.element.BlockData;
import slimeknights.mantle.client.book.data.element.DataLocation;
import slimeknights.mantle.client.book.data.element.ItemStackData;
import slimeknights.mantle.client.book.repository.BookRepository;

@SideOnly(Side.CLIENT)
public class PageData implements IDataItem {

  private static final transient ArrayList<ValueHotswap> hotswaps = new ArrayList<>();

  public String name = null;
  public String type = "";
  public String data = "";
  public float scale = 1.0F;

  public transient SectionData parent;
  public transient BookRepository source;
  public transient PageContent content;

  public PageData() {
    this(false);
  }

  public PageData(boolean custom) {
    if(custom) {
      data = "no-load";
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public void load() {
    if(name == null) {
      name = "page" + parent.unnamedPageCounter++;
    }

    name = name.toLowerCase();

    if(!data.equals("no-load")) {
      IResource pageInfo = source.getResource(source.getResourceLocation(data));
      if(pageInfo != null) {
        String data = source.resourceToString(pageInfo);
        if(!data.isEmpty()) {
          Class<? extends PageContent> ctype = BookLoader.getPageType(type);

          try {
            content = BookLoader.GSON.fromJson(data, ctype);
          } catch(Exception e) {
            content = new ContentError(ctype == null ? "Failed to create a page of type \"" + type + "\", perhaps the type is not registered?" : "Failed to create a page of type \"" + type + "\", perhaps the page file \"" + this.data + "\" is missing or invalid?", e);
          }
        }
      }
    }

    if(content == null) {
      try {
        content = BookLoader.getPageType(type).newInstance();
      } catch(InstantiationException | IllegalAccessException | NullPointerException e) {
        content = new ContentError("Failed to create a page of type \"" + type + "\", perhaps the type is not registered?");
      }
    }

    try {
      content.parent = this;
      content.load();
    } catch(Exception e) {
      content = new ContentError("Failed to load page " + parent.name + "." + name + ".", e);
      e.printStackTrace();
    }

    content.source = source;

    for(Field f : content.getClass().getFields()) {
      if(Modifier.isTransient(f.getModifiers()) || Modifier.isStatic(f.getModifiers()) || Modifier
          .isFinal(f.getModifiers())) {
        continue;
      }

      try {
        if(f.get(content) == null) {
          continue;
        }
      } catch(IllegalAccessException e) {
        e.printStackTrace();
      }

      for(ValueHotswap swap : hotswaps) {
        Class<?> c = f.getType();

        if(c.isArray() && c.getComponentType().isAssignableFrom(swap.t)) {
          try {
            f.setAccessible(true);
            Object[] o = (Object[]) f.get(content);

            for(Object ob : o) {
              swap.swap(source, ob);
            }
          } catch(IllegalAccessException e) {
            e.printStackTrace();
          }
        } else if(swap.t.isAssignableFrom(c)) {
          try {
            f.setAccessible(true);
            Object o = f.get(content);

            swap.swap(source, o);
          } catch(IllegalAccessException e) {
            e.printStackTrace();
          }
        }
      }
    }
  }

  public static void addSwap(Class<?> t, Class<? extends ValueHotswap> swap) {
    try {
      ValueHotswap hotswap = swap.newInstance();
      hotswap.t = t;

      hotswaps.add(hotswap);
    } catch(InstantiationException | IllegalAccessException e) {
      e.printStackTrace();
    }
  }

  public String getTitle() {
    String title = parent.parent.strings.get(parent.name + "." + name);
    return title == null ? name : title;
  }

  static {
    addSwap(DataLocation.class, new ValueHotswap<DataLocation>() {
      @Override
      public void swap(BookRepository source, DataLocation object) {
        object.location = object.file == "$BLOCK_ATLAS" ? TextureMap.LOCATION_BLOCKS_TEXTURE : source
            .getResourceLocation(object.file, true);
      }
    }.getClass());
    addSwap(ItemStackData.class, new ValueHotswap<ItemStackData>() {
      @Override
      public void swap(BookRepository source, ItemStackData object) {
        object.source = source;
        object.itemListLocation = source.getResourceLocation(object.itemList);

        if(object.itemListLocation != null) {
          object.id = "->itemList";
        }
      }
    }.getClass());
    addSwap(BlockData.class, new ValueHotswap<BlockData>() {
      @Override
      public void swap(BookRepository source, BlockData object) {
        if(object.endPos == null) {
          object.endPos = object.pos;
        }
      }
    }.getClass());
  }

  public static abstract class ValueHotswap<T> {

    protected Class<?> t;

    public abstract void swap(BookRepository source, T object);
  }
}
