package slimeknights.mantle.client.book.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import slimeknights.mantle.client.book.BookLoader;
import slimeknights.mantle.client.book.BookTransformer;
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
    for (BookRepository repo : repositories) {
      List<SectionData> repoContents = repo.getSections();
      sections.addAll(repoContents);

      if (repo.hasAppearanceData) {
        ResourceLocation appearanceLocation = getResourceLocation("appearance.json");

        if (resourceExists(appearanceLocation))
          appearance = BookLoader.GSON.fromJson(resourceToString(getResource(appearanceLocation)), AppearanceData.class);
        else
          appearance = new AppearanceData();

        appearance.load();

        ResourceLocation itemLinkLocation = getResourceLocation("items.json");

        if (resourceExists(itemLinkLocation)) {
          itemLinks = new ArrayList<>(Arrays.asList(BookLoader.GSON.fromJson(resourceToString(getResource(itemLinkLocation)), ItemStackData.ItemLink[].class)));
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
    for (SectionData section : sections)
      if (section.name.equals(name.toLowerCase()))
        return section;

    return null;
  }

  public int getFirstPageNumber(SectionData section) {
    int pages = 0;
    for (SectionData sect : sections) {
      if (section == sect)
        return pages + 1;

      pages += sect.getPageCount();
    }

    return -1;
  }

  public PageData findPage(int number) {
    if (number < 0)
      return null;

    int pages = 0;
    for (SectionData section : sections) {
      if (pages + section.getPageCount() > number)
        return section.pages.get(number - pages);
      else
        pages += section.getPageCount();
    }

    return null;
  }

  public PageData findPage(String location) {
    return findPage(findPageNumber(location));
  }

  public int findPageNumber(String location) {
    location = location.toLowerCase();

    int pages = 0;

    if (!location.contains("."))
      return -1;

    String sectionName = location.substring(0, location.indexOf('.'));
    String pageName = location.substring(location.indexOf('.') + 1);

    for (SectionData section : sections) {
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
    int pages = 0;
    for (SectionData section : sections) {
      pages += section.getPageCount();
    }
    return pages;
  }

  public int getFullPageCount() {
    return (int) Math.ceil((getPageCount() - 1) / 2F) + 1;
  }

  public String getItemAction(ItemStackData item) {
    for (ItemStackData.ItemLink link : itemLinks) {
      if (item.id.equals(link.item.id) && (!link.damageSensitive || item.damage == link.item.damage))
        return link.action;
    }

    return "";
  }

  public void openGui(@Nullable ItemStack item) {
    if (Minecraft.getMinecraft().currentScreen == null)
      Minecraft.getMinecraft().displayGuiScreen(new GuiBook(this, item));
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
