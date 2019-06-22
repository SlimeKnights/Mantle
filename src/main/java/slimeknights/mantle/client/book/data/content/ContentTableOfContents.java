package slimeknights.mantle.client.book.data.content;

import net.minecraft.client.Minecraft;

import java.util.ArrayList;

import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.data.element.TextData;
import slimeknights.mantle.client.gui.book.GuiBook;
import slimeknights.mantle.client.gui.book.element.BookElement;
import slimeknights.mantle.client.gui.book.element.ElementText;

public class ContentTableOfContents extends PageContent {

  public String title;
  public TextData[] data;

  public ContentTableOfContents(String title, TextData... contents) {
    this.title = title;
    this.data = contents;
  }

  @Override
  public void build(BookData book, ArrayList<BookElement> list, boolean rightSide) {
    int y = 0;

    if(this.title != null && !this.title.trim().isEmpty()) {
      this.addTitle(list, this.title);
      y += TITLE_HEIGHT;
    }

    for(int i = 0; i < this.data.length; i++) {
      TextData text = this.data[i];
      list.add(new ElementText(0, y + i * (int) (Minecraft
                                                     .getInstance().fontRenderer.FONT_HEIGHT * text.scale), GuiBook.PAGE_WIDTH, Minecraft
                                   .getInstance().fontRenderer.FONT_HEIGHT, text));
    }
  }
}
