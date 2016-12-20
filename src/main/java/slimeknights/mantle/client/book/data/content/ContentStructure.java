package slimeknights.mantle.client.book.data.content;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;

import java.util.ArrayList;

import slimeknights.mantle.client.book.BookLoader;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.data.element.BlockData;
import slimeknights.mantle.client.book.repository.BookRepository;
import slimeknights.mantle.client.gui.book.GuiArrow;
import slimeknights.mantle.client.gui.book.GuiBook;
import slimeknights.mantle.client.gui.book.element.BookElement;
import slimeknights.mantle.client.gui.book.element.ElementAnimationToggle;
import slimeknights.mantle.client.gui.book.element.ElementArrow;
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
      boolean showButtons = size[1] > 1;
      if(showButtons) {
        //structureSizeX -= GuiArrow.ArrowType.REFRESH.w;
      }
      ElementStructure elementStructure = new ElementStructure(offset, y, structureSizeX, structureSizeY, size, structure);
      list.add(elementStructure);

      if(showButtons) {
        int col = book.appearance.structureButtonColor;
        int colHover = book.appearance.structureButtonColorHovered;
        int colToggled = book.appearance.structureButtonColorToggled;

        int midY = y + structureSizeY / 2 - (GuiArrow.ArrowType.UP.h + GuiArrow.ArrowType.DOWN.h) / 2;

        int dx = (GuiArrow.ArrowType.REFRESH.w - GuiArrow.ArrowType.UP.w) / 2;

        //list.add(new ElementArrow(ElementStructure.BUTTON_ID_LAYER_UP, elementStructure, structureSizeX + offset + dx, midY, GuiArrow.ArrowType.UP, col, colHover));
        //midY += GuiArrow.ArrowType.UP.h + 2;
        //list.add(new ElementArrow(ElementStructure.BUTTON_ID_LAYER_DOWN, elementStructure, structureSizeX + offset + dx, midY, GuiArrow.ArrowType.DOWN, col, colHover));

        list.add(new ElementAnimationToggle(ElementStructure.BUTTON_ID_ANIMATE, elementStructure, GuiBook.PAGE_WIDTH - GuiArrow.ArrowType.REFRESH.w, 0, GuiArrow.ArrowType.REFRESH, col, colHover, colToggled));
      }
    }
  }
}
