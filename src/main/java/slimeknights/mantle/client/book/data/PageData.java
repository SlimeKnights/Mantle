package slimeknights.mantle.client.book.data;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.TrueCondition;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.client.book.BookLoader;
import slimeknights.mantle.client.book.data.content.ContentError;
import slimeknights.mantle.client.book.data.content.PageContent;
import slimeknights.mantle.client.book.data.element.IDataElement;
import slimeknights.mantle.client.book.repository.BookRepository;

import javax.annotation.Nullable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Map;

public class PageData implements IDataItem, IConditional {

  public String name = null;
  public ResourceLocation type = Mantle.getResource("blank");
  public String data = "";
  public float scale = 1.0F;
  public ICondition condition = TrueCondition.INSTANCE;

  /** Contains arbitrary data to be used by custom transformers and other things */
  public Map<ResourceLocation, JsonElement> extraData = Collections.emptyMap();

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

  @SuppressWarnings("unused") // utility
  public String translate(String string) {
    return this.parent.translate(string);
  }

  @Override
  public void load() {
    if (this.name == null) {
      this.name = "page" + this.parent.unnamedPageCounter++;
    }

    this.name = this.name.toLowerCase();

    Class<? extends PageContent> ctype = BookLoader.getPageType(type);

    if (!this.data.isEmpty() && !this.data.equals("no-load")) {
      Resource pageInfo = this.source.getResource(this.source.getResourceLocation(this.data));
      if (pageInfo != null) {
        String data = this.source.resourceToString(pageInfo);
        if (!data.isEmpty()) {
          // Test if the page type is specified within the content iteself
          PageTypeOverrider overrider = BookLoader.getGson().fromJson(data, PageTypeOverrider.class);
          if (overrider.type != null) {
            Class<? extends PageContent> overriddenType = BookLoader.getPageType(overrider.type);
            if(overriddenType != null) {
              ctype = BookLoader.getPageType(overrider.type);
              // Also override the type on the page so that we can read it out in transformers
              type = overrider.type;
            }
          }

          if (ctype != null) {
            try {
              this.content = BookLoader.getGson().fromJson(data, ctype);
            } catch (Exception e) {
              this.content = new ContentError("Failed to create a page of type \"" + this.type + "\", perhaps the page file \"" + this.data + "\" is missing or invalid?", e);
              e.printStackTrace();
            }
          } else {
            this.content = new ContentError("Failed to create a page of type \"" + this.type + "\" as it is not registered.");
          }
        }
      }
    }

    if (this.content == null) {
      if (ctype != null) {
        try {
          this.content = ctype.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | NullPointerException | NoSuchMethodException | InvocationTargetException e) {
          this.content = new ContentError("Failed to create a page of type \"" + this.type + "\".", e);
          e.printStackTrace();
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

  /** Gets the title for the page data, which can be overridden by translation */
  public String getTitle() {
    String title = this.parent.parent.strings.get(this.parent.name + "." + this.name);
    if (title != null) {
      return title;
    }
    title = content.getTitle();
    if (title != null && !title.isEmpty()) {
      return title;
    }
    return this.name;
  }

  @Override
  public boolean isConditionMet() {
    return condition.test();
  }

  private static class PageTypeOverrider {
    public ResourceLocation type;
  }
}
