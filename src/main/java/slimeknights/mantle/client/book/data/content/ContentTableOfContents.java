package slimeknights.mantle.client.book.data.content;

import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.data.element.TextData;
import slimeknights.mantle.client.screen.book.BookScreen;
import slimeknights.mantle.client.screen.book.element.BookElement;
import slimeknights.mantle.client.screen.book.element.TextElement;

import java.util.ArrayList;
import net.minecraft.client.MinecraftClient;

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

    if (this.title != null && !this.title.trim().isEmpty()) {
      this.addTitle(list, this.title);
      y += TITLE_HEIGHT;
    }

    for (int i = 0; i < this.data.length; i++) {
      TextData text = this.data[i];
      list.add(new TextElement(0, y + i * (int) (MinecraftClient.getInstance().textRenderer.fontHeight * text.scale), BookScreen.PAGE_WIDTH, MinecraftClient.getInstance().textRenderer.fontHeight, text));
    }
  }
}
