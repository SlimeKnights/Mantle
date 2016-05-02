package slimeknights.mantle.client.book.data;

import net.minecraft.client.resources.IResource;
import net.minecraft.stats.Achievement;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.StatFileWriter;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Arrays;

import javax.annotation.Nullable;

import slimeknights.mantle.client.book.BookLoader;
import slimeknights.mantle.client.book.data.content.ContentError;
import slimeknights.mantle.client.book.data.element.ImageData;
import slimeknights.mantle.client.book.repository.BookRepository;

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

  public void update(@Nullable StatFileWriter writer) {
  }

  public String getTitle() {
    String title = parent.strings.get(name);
    return title == null ? name : title;
  }

  public int getPageCount() {
    return pages.size();
  }

  public boolean isUnlocked(StatFileWriter writer) {
    if(writer == null || requirements == null || requirements.length == 0) {
      return true;
    }

    for(String achievement : requirements) {
      if(!requirementSatisfied(achievement, writer)) {
        return false;
      }
    }

    return true;
  }

  public static boolean requirementSatisfied(String requirement, StatFileWriter writer) {
    if(writer == null) {
      return true;
    }

    Achievement achievement = findAchievement(requirement);

    return achievement == null || writer.hasAchievementUnlocked(achievement);

  }

  public static Achievement findAchievement(String name) {
    if(name == null || name.isEmpty()) {
      return null;
    }

    for(Achievement achievement : AchievementList.ACHIEVEMENTS) {
      if(achievement.statId.equals(name)) {
        return achievement;
      }
    }

    return null;
  }
}
