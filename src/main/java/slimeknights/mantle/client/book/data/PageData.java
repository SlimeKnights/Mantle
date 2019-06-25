package slimeknights.mantle.client.book.data;

import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.resources.IResource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import slimeknights.mantle.client.book.BookLoader;
import slimeknights.mantle.client.book.data.content.ContentError;
import slimeknights.mantle.client.book.data.content.PageContent;
import slimeknights.mantle.client.book.data.element.BlockData;
import slimeknights.mantle.client.book.data.element.DataLocation;
import slimeknights.mantle.client.book.data.element.ItemStackData;
import slimeknights.mantle.client.book.repository.BookRepository;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

@OnlyIn(Dist.CLIENT)
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
    if (custom) {
      this.data = "no-load";
    }
  }

  public String translate(String string) {
    return this.parent.translate(string);
  }

  @Override
  @SuppressWarnings("unchecked")
  public void load() {
    if (this.name == null) {
      this.name = "page" + this.parent.unnamedPageCounter++;
    }

    this.name = this.name.toLowerCase();

    if (!this.data.equals("no-load")) {
      IResource pageInfo = this.source.getResource(this.source.getResourceLocation(this.data));
      if (pageInfo != null) {
        String data = this.source.resourceToString(pageInfo);
        if (!data.isEmpty()) {
          Class<? extends PageContent> ctype = BookLoader.getPageType(this.type);

          try {
            this.content = BookLoader.GSON.fromJson(data, ctype);
          }
          catch (Exception e) {
            this.content = new ContentError(ctype == null ? "Failed to create a page of type \"" + this.type + "\", perhaps the type is not registered?" : "Failed to create a page of type \"" + this.type + "\", perhaps the page file \"" + this.data + "\" is missing or invalid?", e);
          }
        }
      }
    }

    if (this.content == null) {
      try {
        this.content = BookLoader.getPageType(this.type).newInstance();
      }
      catch (InstantiationException | IllegalAccessException | NullPointerException e) {
        this.content = new ContentError("Failed to create a page of type \"" + this.type + "\", perhaps the type is not registered?");
      }
    }

    try {
      this.content.parent = this;
      this.content.load();
    }
    catch (Exception e) {
      this.content = new ContentError("Failed to load page " + this.parent.name + "." + this.name + ".", e);
      e.printStackTrace();
    }

    this.content.source = this.source;

    for (Field f : this.content.getClass().getFields()) {
      if (Modifier.isTransient(f.getModifiers()) || Modifier.isStatic(f.getModifiers()) || Modifier
              .isFinal(f.getModifiers())) {
        continue;
      }

      try {
        if (f.get(this.content) == null) {
          continue;
        }
      }
      catch (IllegalAccessException e) {
        e.printStackTrace();
      }

      for (ValueHotswap swap : hotswaps) {
        Class<?> c = f.getType();

        if (c.isArray() && c.getComponentType().isAssignableFrom(swap.t)) {
          try {
            f.setAccessible(true);
            Object[] o = (Object[]) f.get(this.content);

            for (Object ob : o) {
              swap.swap(this.source, ob);
            }
          }
          catch (IllegalAccessException e) {
            e.printStackTrace();
          }
        }
        else if (swap.t.isAssignableFrom(c)) {
          try {
            f.setAccessible(true);
            Object o = f.get(this.content);

            swap.swap(this.source, o);
          }
          catch (IllegalAccessException e) {
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
    }
    catch (InstantiationException | IllegalAccessException e) {
      e.printStackTrace();
    }
  }

  public String getTitle() {
    String title = this.parent.parent.strings.get(this.parent.name + "." + this.name);
    return title == null ? this.name : title;
  }

  static {
    addSwap(DataLocation.class, new ValueHotswap<DataLocation>() {
      @Override
      public void swap(BookRepository source, DataLocation object) {
        object.location = object.file == "$BLOCK_ATLAS" ? AtlasTexture.LOCATION_BLOCKS_TEXTURE : source
                .getResourceLocation(object.file, true);
      }
    }.getClass());
    addSwap(ItemStackData.class, new ValueHotswap<ItemStackData>() {
      @Override
      public void swap(BookRepository source, ItemStackData object) {
        object.source = source;
        object.itemListLocation = source.getResourceLocation(object.itemList);

        if (object.itemListLocation != null) {
          object.id = "->itemList";
        }
      }
    }.getClass());
    addSwap(BlockData.class, new ValueHotswap<BlockData>() {
      @Override
      public void swap(BookRepository source, BlockData object) {
        if (object.endPos == null) {
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
