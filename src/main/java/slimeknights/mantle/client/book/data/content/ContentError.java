package slimeknights.mantle.client.book.data.content;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;

import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.data.element.TextData;
import slimeknights.mantle.client.gui.book.GuiBook;
import slimeknights.mantle.client.gui.book.element.BookElement;
import slimeknights.mantle.client.gui.book.element.ElementText;

@SideOnly(Side.CLIENT)
public class ContentError extends PageContent {

  private String errorStage;
  private Exception exception;

  public ContentError(String errorStage) {
    this.errorStage = errorStage;
  }

  public ContentError(String errorStage, Exception e) {
    this(errorStage);
    this.exception = e;
  }

  @Override
  public void build(BookData book, ArrayList<BookElement> list, boolean rightSide) {
    addTitle(list, "Error");

    TextData[] text = new TextData[1 + (exception != null ? 2 : 0)];
    text[0] = new TextData(errorStage);
    text[0].color = "dark_red";
    text[0].underlined = true;

    if(exception != null) {
      text[1] = new TextData("The following error has occured: ");
      text[1].color = "dark_red";
      text[1].paragraph = true;

      text[2] = new TextData(exception.getMessage() != null ? exception.getMessage() : exception.getClass()
                                                                                                .getSimpleName());
      text[2].color = "dark_red";
      text[2].paragraph = true;
    }

    list.add(new ElementText(0, TITLE_HEIGHT, GuiBook.PAGE_WIDTH, GuiBook.PAGE_HEIGHT - TITLE_HEIGHT, text));
  }
}
