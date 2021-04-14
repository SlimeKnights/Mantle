package slimeknights.mantle.client.book.data;

import net.minecraft.resources.IResource;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModLoadingContext;
import slimeknights.mantle.client.book.BookLoader;
import slimeknights.mantle.client.book.data.content.ContentError;
import slimeknights.mantle.client.book.data.content.PageContent;
import slimeknights.mantle.client.book.data.element.IDataElement;
import slimeknights.mantle.client.book.repository.BookRepository;

import javax.annotation.Nullable;
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

  @SuppressWarnings("unused") // used implicitly by GSON
  public PageData() {
    this(false);
  }

  public PageData(boolean custom) {
    if (custom) {
      this.data = "no-load";
    }
  }

  @Override
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

          if (ctype != null) {
            try {
              this.content = BookLoader.GSON.fromJson(data, ctype);
            } catch (Exception e) {
              this.content = new ContentError("Failed to create a page of type \"" + this.type + "\", perhaps the page file \"" + this.data + "\" is missing or invalid?", e);
            }
          } else {
            this.content = new ContentError("Failed to create a page of type \"" + this.type + "\" as it is not registered.");
          }
        }
      }
    }

    if (this.content == null) {
      Class<? extends PageContent> ctype = BookLoader.getPageType(this.type);

      if (ctype != null) {
        try {
          this.content = ctype.newInstance();
        } catch (InstantiationException | IllegalAccessException | NullPointerException e) {
          this.content = new ContentError("Failed to create a page of type \"" + this.type + "\".", e);
        }
      } else {
        this.content = new ContentError("Failed to create a page of type \"" + this.type + "\" as it is not registered.");
      }
    }

    try {
      this.content.parent = this;
      this.content.load();
    } catch (Exception e) {
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

  private void processObject(@Nullable Object o, final int depth) {
    if (depth > 4 || o == null)
      return;

    Class<?> c = o.getClass();
    boolean isArray = c.isArray();

    if (!isArray) {
      if (IDataElement.class.isAssignableFrom(c)) {
        ((IDataElement) o).load(this.source);
      }
      return;
    }

    for (int i = 0; i < Array.getLength(o); i++) {
      this.processObject(Array.get(o, i), depth + 1);
    }
  }

  public IFormattableTextComponent getTitle() {
    return new TranslationTextComponent(ModLoadingContext.get().getActiveContainer().getNamespace() + "." + this.parent.name + "." + this.name);
  }
}
