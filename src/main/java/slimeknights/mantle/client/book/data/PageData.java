package slimeknights.mantle.client.book.data;

import net.minecraft.resources.IResource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import slimeknights.mantle.client.book.BookLoader;
import slimeknights.mantle.client.book.data.content.ContentError;
import slimeknights.mantle.client.book.data.content.PageContent;
import slimeknights.mantle.client.book.data.element.IDataElement;
import slimeknights.mantle.client.book.repository.BookRepository;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@OnlyIn(Dist.CLIENT)
public class PageData implements IDataItem {

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
      this.processField(f);
    }
  }

  private void processField(Field f) {
    f.setAccessible(true);

    if (Modifier.isTransient(f.getModifiers()) || Modifier.isStatic(f.getModifiers()) || Modifier
            .isFinal(f.getModifiers())) {
      return;
    }

    try {
      Object o = f.get(this.content);
      if (o != null) {
        this.processObject(o, 0);
      }
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
  }

  private void processObject(Object o, final int depth) {
    if(depth > 4 || o == null)
      return;

    Class<?> c = o.getClass();
    boolean isArray = c.isArray();

    if(!isArray) {
      if(IDataElement.class.isAssignableFrom(c)) {
        ((IDataElement)o).load(this.source);
      }
      return;
    }

    for(int i = 0; i < Array.getLength(o); i++){
      this.processObject(Array.get(o, i), depth + 1);
    }
  }

  public String getTitle() {
    String title = this.parent.parent.strings.get(this.parent.name + "." + this.name);
    return title == null ? this.name : title;
  }
}
