package slimeknights.mantle.client.book.data;

import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.resources.IResource;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import slimeknights.mantle.client.book.BookLoader;
import slimeknights.mantle.client.book.data.content.ContentError;
import slimeknights.mantle.client.book.data.element.ImageData;
import slimeknights.mantle.client.book.repository.BookRepository;
import slimeknights.mantle.client.gui.book.GuiBook;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;

@SideOnly(Side.CLIENT)
public class SectionData implements IDataItem {

  public String name = null;
  public ImageData icon = new ImageData();
  public String[] requirements = new String[0];
  public boolean hideWhenLocked = false;
  public String data = "";

  public transient int unnamedPageCounter = 0;
  public transient BookData parent;
  public transient BookRepository source;
  public transient ArrayList<PageData> pages = new ArrayList<>();

  public SectionData() {
    this(false);
  }

  public SectionData(boolean custom) {
    if(custom) {
      data = "no-load";
    }
  }

  public String translate(String string) {
    return parent.translate(string);
  }

  @Override
  public void load() {
    if(name == null) {
      name = "section" + parent.unnamedSectionCounter++;
    }

    name = name.toLowerCase();

    if(!data.equals("no-load")) {
      IResource pagesInfo = source.getResource(source.getResourceLocation(data));
      if(pagesInfo != null) {
        String data = source.resourceToString(pagesInfo);
        if(!data.isEmpty()) {
          try {
            pages = new ArrayList<>(Arrays.asList(BookLoader.GSON.fromJson(data, PageData[].class)));
          } catch(Exception e) {
            pages = new ArrayList<>();
            PageData pdError = new PageData(true);
            pdError.name = "errorrenous";
            pdError.content = new ContentError("Failed to load section " + name + ".", e);
            pages.add(pdError);
          }
        }
      }
    }

    for(PageData page : pages) {
      page.parent = this;
      page.source = source;
      page.load();
    }

    icon.location = source.getResourceLocation(icon.file, true);
  }

  public void update(@Nullable GuiBook.AdvancementCache advancementCache) {
  }

  public String getTitle() {
    String title = parent.strings.get(name);
    return title == null ? name : title;
  }

  public int getPageCount() {
    return pages.size();
  }

  public boolean isUnlocked(GuiBook.AdvancementCache advancementCache) {
    if(advancementCache == null || requirements == null || requirements.length == 0) {
      return true;
    }

    for(String achievement : requirements) {
      if(!requirementSatisfied(achievement, advancementCache)) {
        return false;
      }
    }

    return true;
  }

  public static boolean requirementSatisfied(String requirement, GuiBook.AdvancementCache advancementCache) {
    if(advancementCache == null) {
      return true;
    }

    AdvancementProgress progress = advancementCache.getProgress(requirement);

    return progress != null && progress.isDone();

  }
}
