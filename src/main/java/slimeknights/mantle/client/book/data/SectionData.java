package slimeknights.mantle.client.book.data;

import com.google.common.collect.Sets;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.IResource;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModLoadingContext;
import slimeknights.mantle.client.book.BookLoader;
import slimeknights.mantle.client.book.data.content.ContentError;
import slimeknights.mantle.client.book.data.element.ImageData;
import slimeknights.mantle.client.book.repository.BookRepository;
import slimeknights.mantle.client.screen.book.BookScreen;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

@OnlyIn(Dist.CLIENT)
public class SectionData implements IDataItem {

  public String name = null;
  public ImageData icon = new ImageData();
  public Set<String> requirements = Sets.newHashSet();
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
    if (custom) {
      this.data = "no-load";
    }
  }

  @Override
  public void load() {
    if (this.name == null) {
      this.name = "section" + this.parent.unnamedSectionCounter++;
    }

    this.name = this.name.toLowerCase();

    if (!this.data.equals("no-load")) {
      IResource pagesInfo = this.source.getResource(this.source.getResourceLocation(this.data));
      if (pagesInfo != null) {
        String data = this.source.resourceToString(pagesInfo);
        if (!data.isEmpty()) {
          try {
            this.pages = this.getPages(data);
          } catch (Exception e) {
            this.pages = new ArrayList<>();
            PageData pdError = new PageData(true);
            pdError.name = "errorrenous";
            pdError.content = new ContentError("Failed to load section " + this.name + ".", e);
            this.pages.add(pdError);

            e.printStackTrace();
          }
        }
      }
    }

    for (PageData page : this.pages) {
      page.parent = this;
      page.source = this.source;
      page.load();
    }

    this.icon.load(this.source);
  }

  /**
   * Gets a list of pages from the given data
   *
   * @param data JSON data
   * @return ArrayList of pages for the book
   */
  protected ArrayList<PageData> getPages(String data) {
    return new ArrayList<>(Arrays.asList(BookLoader.GSON.fromJson(data, PageData[].class)));
  }

  public void update(@Nullable BookScreen.AdvancementCache advancementCache) {
  }

  public IFormattableTextComponent getTitle() {
    return new TranslationTextComponent(ModLoadingContext.get().getActiveContainer().getNamespace() + "." + "book.section." + this.name);
  }

  public int getPageCount() {
    return this.pages.size();
  }

  public boolean isUnlocked(@Nullable BookScreen.AdvancementCache advancementCache) {
    if (advancementCache == null || this.requirements == null || this.requirements.size() == 0) {
      return true;
    }

    for (String achievement : this.requirements) {
      if (!requirementSatisfied(achievement, advancementCache)) {
        return false;
      }
    }

    return true;
  }

  public static boolean requirementSatisfied(String requirement, @Nullable BookScreen.AdvancementCache advancementCache) {
    if (advancementCache == null) {
      return true;
    }

    AdvancementProgress progress = advancementCache.getProgress(requirement);

    return progress != null && progress.isDone();

  }
}
