package slimeknights.mantle.client.book.data.content;

import java.util.ArrayList;
import net.minecraft.util.ResourceLocation;
import slimeknights.mantle.client.book.BookLoader;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.data.element.BlockData;
import slimeknights.mantle.client.book.repository.BookRepository;
import slimeknights.mantle.client.gui.book.GuiBook;
import slimeknights.mantle.client.gui.book.element.BookElement;
import slimeknights.mantle.client.gui.book.element.ElementStructure;
import slimeknights.mantle.client.gui.book.element.ElementText;

public class ContentStructure extends PageContent {

  public String title;
  public String data;

  public int[] size;
  public BlockData[] structure;

  @Override
  public void load() {
    BookRepository repo = parent.source;

    if (data == null || data.isEmpty())
      return;

    ResourceLocation location = repo.getResourceLocation(data);

    if (location != null && repo.resourceExists(location)) {
      ContentStructure structure = BookLoader.GSON.fromJson(repo.resourceToString(repo.getResource(location)), ContentStructure.class);
      structure.parent = parent;
      structure.load();
      size = structure.size;
      this.structure = structure.structure;
    }
  }

  @Override
  public void build(BookData book, ArrayList<BookElement> list) {
    int y = TITLE_HEIGHT;
    if (title == null || title.isEmpty())
      y = 0;
    else
      addTitle(list, title);

    if (size != null && size.length == 3 && structure != null && structure.length > 0)
      list.add(new ElementStructure(0, y, GuiBook.PAGE_WIDTH, GuiBook.PAGE_HEIGHT - y - 10, size, structure));

    list.add(new ElementText(0, GuiBook.PAGE_HEIGHT - 10, GuiBook.PAGE_WIDTH, 10, "WIP - Not Yet Implemented"));
  }
}
