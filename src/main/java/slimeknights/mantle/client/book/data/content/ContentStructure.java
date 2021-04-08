package slimeknights.mantle.client.book.data.content;

import slimeknights.mantle.client.book.BookLoader;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.data.element.BlockData;
import slimeknights.mantle.client.book.repository.BookRepository;
import slimeknights.mantle.client.screen.book.ArrowButton;
import slimeknights.mantle.client.screen.book.BookScreen;
import slimeknights.mantle.client.screen.book.element.BookElement;
import slimeknights.mantle.client.screen.book.element.AnimationToggleElement;
import slimeknights.mantle.client.screen.book.element.StructureElement;
import slimeknights.mantle.client.screen.book.element.TextElement;

import java.util.ArrayList;
import net.minecraft.util.ChatUtil;
import net.minecraft.util.Identifier;

public class ContentStructure extends PageContent {

  public String title;
  public String data;

  public int[] size;
  public BlockData[] structure;
  public String text;

  @Override
  public void load() {
    BookRepository repo = this.parent.source;

    if (this.data == null || this.data.isEmpty()) {
      return;
    }

    Identifier location = repo.getResourceLocation(this.data);

    if (location != null && repo.resourceExists(location)) {
      ContentStructure structure = BookLoader.GSON.fromJson(repo.resourceToString(repo.getResource(location)), ContentStructure.class);
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
    if (this.title == null || this.title.isEmpty()) {
      y = 0;
    }
    else {
      this.addTitle(list, this.title);
    }

    int offset = 0;
    int structureSizeX = BookScreen.PAGE_WIDTH;
    int structureSizeY = BookScreen.PAGE_HEIGHT - y - 10;

    if (!ChatUtil.isEmpty(this.text)) {
      offset = 15;
      structureSizeX -= 2 * offset;
      structureSizeY -= 2 * offset;

      list.add(new TextElement(0, BookScreen.PAGE_HEIGHT - 10 - 2 * offset, BookScreen.PAGE_WIDTH, 2 * offset, this.text));
    }

    if (this.size != null && this.size.length == 3 && this.structure != null && this.structure.length > 0) {
      boolean showButtons = this.size[1] > 1;
      if (showButtons) {
        //structureSizeX -= ArrowButton.ArrowType.REFRESH.w;
      }
      StructureElement structureElement = new StructureElement(offset, y, structureSizeX, structureSizeY, this.size, this.structure);
      list.add(structureElement);

      if (showButtons) {
        int col = book.appearance.structureButtonColor;
        int colHover = book.appearance.structureButtonColorHovered;
        int colToggled = book.appearance.structureButtonColorToggled;

        int midY = y + structureSizeY / 2 - (ArrowButton.ArrowType.UP.h + ArrowButton.ArrowType.DOWN.h) / 2;

        int dx = (ArrowButton.ArrowType.REFRESH.w - ArrowButton.ArrowType.UP.w) / 2;

        //list.add(new ElementArrow(ElementStructure.BUTTON_ID_LAYER_UP, elementStructure, structureSizeX + offset + dx, midY, ArrowButton.ArrowType.UP, col, colHover));
        //midY += ArrowButton.ArrowType.UP.h + 2;
        //list.add(new ElementArrow(ElementStructure.BUTTON_ID_LAYER_DOWN, elementStructure, structureSizeX + offset + dx, midY, ArrowButton.ArrowType.DOWN, col, colHover));

        list.add(new AnimationToggleElement(BookScreen.PAGE_WIDTH - ArrowButton.ArrowType.REFRESH.w, 0, ArrowButton.ArrowType.REFRESH, col, colHover, colToggled, structureElement));
      }
    }
  }
}
