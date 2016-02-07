package slimeknights.mantle.client.book.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatFileWriter;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import slimeknights.mantle.client.book.BookLoader;
import slimeknights.mantle.client.book.BookTransformer;
import slimeknights.mantle.client.book.data.content.ContentError;
import slimeknights.mantle.client.book.data.element.ItemStackData;
import slimeknights.mantle.client.book.repository.BookRepository;
import slimeknights.mantle.client.gui.book.GuiBook;
import static slimeknights.mantle.client.book.ResourceHelper.getResource;
import static slimeknights.mantle.client.book.ResourceHelper.getResourceLocation;
import static slimeknights.mantle.client.book.ResourceHelper.resourceExists;
import static slimeknights.mantle.client.book.ResourceHelper.resourceToString;

@SideOnly(Side.CLIENT)
public class BookData implements IDataItem {

  public transient int unnamedSectionCounter = 0;
  public transient ArrayList<SectionData> sections = new ArrayList<>();
  public transient AppearanceData appearance = new AppearanceData();
  public transient ArrayList<ItemStackData.ItemLink> itemLinks = new ArrayList<>();

  protected final transient ArrayList<BookTransformer> transformers = new ArrayList<>();

  private ArrayList<BookRepository> repositories;

  public BookData(BookRepository... repositories) {
    this.repositories = new ArrayList<>(Arrays.asList(repositories));
  }

  @Override
  public void load() {
    sections.clear();
    appearance = new AppearanceData();
    itemLinks.clear();

    for (BookRepository repo : repositories) {
      try {
        List<SectionData> repoContents = repo.getSections();
        sections.addAll(repoContents);
      } catch (Exception e) {
        SectionData error = new SectionData();
        error.name = "errorenous";
        PageData page = new PageData(true);
        page.name = "errorenous";
        page.content = new ContentError("Failed to load repository " + repo.toString() + ".", e);
        error.pages.add(page);
        sections.add(error);
      }

      if (repo.hasAppearanceData) {
        ResourceLocation appearanceLocation = getResourceLocation("appearance.json");

        if (resourceExists(appearanceLocation))
          try {
            appearance = BookLoader.GSON.fromJson(resourceToString(getResource(appearanceLocation)), AppearanceData.class);
          } catch (Exception ignored) {
          }
        else
          appearance = new AppearanceData();

        appearance.load();

        ResourceLocation itemLinkLocation = getResourceLocation("items.json");

        if (resourceExists(itemLinkLocation)) {
          try {
            itemLinks = new ArrayList<>(Arrays.asList(BookLoader.GSON.fromJson(resourceToString(getResource(itemLinkLocation)), ItemStackData.ItemLink[].class)));
          } catch (Exception ignored) {
          }
        }
      }
    }

    for (BookTransformer transformer : transformers)
      transformer.transform(this);

    for (SectionData section : sections) {
      section.parent = this;
      section.load();
    }
  }

  public SectionData findSection(String name) {
    return findSection(name, null);
  }

  public SectionData findSection(String name, @Nullable StatFileWriter writer) {
    for (SectionData section : sections) {
      section.update(writer);

      if (section.name.equals(name.toLowerCase()))
        return section.isUnlocked(writer) ? section : null;
    }

    return null;
  }

  public int getFirstPageNumber(SectionData section) {
    return getFirstPageNumber(section, null);
  }

  public int getFirstPageNumber(SectionData section, @Nullable StatFileWriter writer) {
    int pages = 0;
    for (SectionData sect : sections) {
      sect.update(writer);

      if (section == sect)
        return section.isUnlocked(writer) ? pages + 1 : -1;

      if (!sect.isUnlocked(writer))
        continue;

      pages += sect.getPageCount();
    }

    return -1;
  }

  public PageData findPage(int number) {
    return findPage(number, null);
  }

  public PageData findPage(int number, @Nullable StatFileWriter writer) {
    if (number < 0)
      return null;

    int pages = 0;
    for (SectionData section : sections) {
      section.update(writer);

      if (!section.isUnlocked(writer))
        continue;

      if (pages + section.getPageCount() > number)
        return section.pages.get(number - pages);
      else
        pages += section.getPageCount();
    }

    return null;
  }

  public PageData findPage(String location) {
    return findPage(location, null);
  }

  public PageData findPage(String location, @Nullable StatFileWriter writer) {
    return findPage(findPageNumber(location, writer));
  }

  public int findPageNumber(String location) {
    return findPageNumber(location, null);
  }

  public int findPageNumber(String location, @Nullable StatFileWriter writer) {
    location = location.toLowerCase();

    int pages = 0;

    if (!location.contains("."))
      return -1;

    String sectionName = location.substring(0, location.indexOf('.'));
    String pageName = location.substring(location.indexOf('.') + 1);

    for (SectionData section : sections) {
      section.update(writer);

      if (!section.isUnlocked(writer))
        continue;

      if (!sectionName.equals(section.name)) {
        pages += section.getPageCount();
        continue;
      }

      for (PageData page : section.pages) {
        if (!pageName.equals(page.name)) {
          pages++;
          continue;
        }

        return pages + 1;
      }
    }

    return -1;
  }

  public int getPageCount() {
    return getPageCount(null);
  }

  public int getPageCount(@Nullable StatFileWriter writer) {
    int pages = 0;
    for (SectionData section : sections) {
      section.update(writer);

      pages += section.isUnlocked(writer) ? section.getPageCount() : 0;
    }
    return pages;
  }

  public int getFullPageCount() {
    return getFullPageCount(null);
  }

  public int getFullPageCount(@Nullable StatFileWriter writer) {
    return (int) Math.ceil((getPageCount(writer) - 1) / 2F) + 1;
  }

  public String getItemAction(ItemStackData item) {
    for (ItemStackData.ItemLink link : itemLinks) {
      if (item.id.equals(link.item.id) && (!link.damageSensitive || item.damage == link.item.damage))
        return link.action;
    }

    return "";
  }

  public List<SectionData> getVisibleSections(StatFileWriter writer) {
    List<SectionData> visible = new ArrayList<>();

    for (SectionData section : sections) {
      if (section.isUnlocked(writer) || !section.hideWhenLocked)
        visible.add(section);
    }

    return visible;
  }

  public void openGui(@Nullable ItemStack item) {
    if (Minecraft.getMinecraft().thePlayer != null)
      Minecraft.getMinecraft().displayGuiScreen(new GuiBook(this, Minecraft.getMinecraft().thePlayer.getStatFileWriter(), item));
  }

  public void addRepository(BookRepository repository) {
    if (repository != null && !this.repositories.contains(repository))
      this.repositories.add(repository);
  }

  public void addTransformer(BookTransformer transformer) {
    if (transformer != null && !transformers.contains(transformer))
      transformers.add(transformer);
  }
}
