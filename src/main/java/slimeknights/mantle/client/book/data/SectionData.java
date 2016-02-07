package slimeknights.mantle.client.book.data;

import java.util.ArrayList;
import java.util.Arrays;
import net.minecraft.client.resources.IResource;
import net.minecraft.stats.Achievement;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.StatFileWriter;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import slimeknights.mantle.client.book.BookLoader;
import slimeknights.mantle.client.book.data.content.ContentError;
import slimeknights.mantle.client.book.data.element.ImageData;
import static slimeknights.mantle.client.book.ResourceHelper.getResource;
import static slimeknights.mantle.client.book.ResourceHelper.getResourceLocation;
import static slimeknights.mantle.client.book.ResourceHelper.resourceToString;

@SideOnly(Side.CLIENT)
public class SectionData implements IDataItem {

  public String name = null;
  public String title = "";
  public ImageData icon = new ImageData();
  public String[] requirements = new String[0];
  public String data = "";

  public transient int unnamedPageCounter = 0;
  public transient BookData parent;
  public transient ArrayList<PageData> pages = new ArrayList<>();

  public SectionData() {
    this(false);
  }

  public SectionData(boolean custom) {
    if (custom)
      data = "no-load";
  }

  @Override
  public void load() {
    if (name == null)
      name = "section" + parent.unnamedSectionCounter++;

    name = name.toLowerCase();

    if (!data.equals("no-load")) {
      IResource pagesInfo = getResource(getResourceLocation(data));
      if (pagesInfo != null) {
        String data = resourceToString(pagesInfo);
        if (!data.isEmpty())
          try {
            pages = new ArrayList<>(Arrays.asList(BookLoader.GSON.fromJson(data, PageData[].class)));
          } catch (Exception e) {
            pages = new ArrayList<>();
            PageData pdError = new PageData(true);
            pdError.name = "errorrenous";
            pdError.content = new ContentError("Failed to load section " + name + ".", e);
            pages.add(pdError);
          }
      }
    }

    for (PageData page : pages) {
      page.parent = this;
      page.load();
    }

    icon.location = getResourceLocation(icon.file, true);
  }

  public int getPageCount() {
    return pages.size();
  }

  public boolean isUnlocked(StatFileWriter writer) {
    if (writer == null || requirements == null || requirements.length == 0)
      return true;

    for (String achievement : requirements) {
      if (!requirementSatisfied(achievement, writer))
        return false;
    }

    return true;
  }

  public static boolean requirementSatisfied(String requirement, StatFileWriter writer) {
    if (writer == null)
      return true;

    Achievement achievement = findAchievement(requirement);

    return achievement == null || writer.hasAchievementUnlocked(achievement);

  }

  public static Achievement findAchievement(String name) {
    if (name == null || name.isEmpty())
      return null;

    for (Achievement achievement : AchievementList.achievementList) {
      if (achievement.statId.equals(name))
        return achievement;
    }

    return null;
  }
}
