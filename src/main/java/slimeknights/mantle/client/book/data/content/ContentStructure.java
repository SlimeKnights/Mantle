package slimeknights.mantle.client.book.data.content;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;

import java.util.ArrayList;

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
  public String text;

  @Override
  public void load() {
    BookRepository repo = parent.source;

    if(data == null || data.isEmpty()) {
      return;
    }

    ResourceLocation location = repo.getResourceLocation(data);

    if(location != null && repo.resourceExists(location)) {
      ContentStructure structure = BookLoader.GSON
          .fromJson(repo.resourceToString(repo.getResource(location)), ContentStructure.class);
      structure.parent = parent;
      structure.load();
      this.size = structure.size;
      this.structure = structure.structure;
      this.text = structure.text;
    }
  }

  @Override
  public void build(BookData book, ArrayList<BookElement> list, boolean rightSide) {
    int y = TITLE_HEIGHT;
    if(title == null || title.isEmpty()) {
      y = 0;
    } else {
      addTitle(list, title);
    }

    int offset = 0;
    int structureSizeX = GuiBook.PAGE_WIDTH;
    int structureSizeY = GuiBook.PAGE_HEIGHT - y - 10;

    if(!StringUtils.isNullOrEmpty(text)) {
      offset = 15;
      structureSizeX -= 2*offset;
      structureSizeY -= 2*offset;

      list.add(new ElementText(0, GuiBook.PAGE_HEIGHT - 10 - 2*offset, GuiBook.PAGE_WIDTH, 2*offset, text));
    }

    if(size != null && size.length == 3 && structure != null && structure.length > 0) {
      list.add(new ElementStructure(offset, y, structureSizeX, structureSizeY, size, structure));
    }
  }
}
