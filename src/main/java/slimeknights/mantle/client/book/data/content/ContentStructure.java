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
    BookRepository repo = this.parent.source;

    if(this.data == null || this.data.isEmpty()) {
      return;
    }

    ResourceLocation location = repo.getResourceLocation(this.data);

    if(location != null && repo.resourceExists(location)) {
      ContentStructure structure = BookLoader.GSON
          .fromJson(repo.resourceToString(repo.getResource(location)), ContentStructure.class);
      structure.parent = this.parent;
      structure.load();
      this.size = structure.size;
      this.structure = structure.structure;
      this.text = structure.text;
    }
  }

  @Override
  public void build(BookData book, ArrayList<BookElement> list, boolean rightSide) {
    int y = TITLE_HEIGHT;
    if(this.title == null || this.title.isEmpty()) {
      y = 0;
    } else {
      this.addTitle(list, this.title);
    }

    int offset = 0;
    int structureSizeX = GuiBook.PAGE_WIDTH;
    int structureSizeY = GuiBook.PAGE_HEIGHT - y - 10;

    if(!StringUtils.isNullOrEmpty(this.text)) {
      offset = 15;
      structureSizeX -= 2*offset;
      structureSizeY -= 2*offset;

      list.add(new ElementText(0, GuiBook.PAGE_HEIGHT - 10 - 2*offset, GuiBook.PAGE_WIDTH, 2*offset, this.text));
    }

    if(this.size != null && this.size.length == 3 && this.structure != null && this.structure.length > 0) {
      boolean showButtons = this.size[1] > 1;
      if(showButtons) {
        //structureSizeX -= GuiArrow.ArrowType.REFRESH.w;
      }
      ElementStructure elementStructure = new ElementStructure(offset, y, structureSizeX, structureSizeY, this.size, this.structure);
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
