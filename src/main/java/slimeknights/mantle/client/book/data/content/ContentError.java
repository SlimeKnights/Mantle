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

    StackTraceElement[] stackTrace = null;
    if(exception != null){
      stackTrace = exception.getStackTrace();
    }

    TextData[] text = new TextData[1 + (exception != null ? 2 : 0) + (stackTrace != null ? 1 + Math.min(stackTrace.length * 2, 8) : 0)];
    text[0] = new TextData(errorStage);
    text[0].color = "dark_red";
    text[0].underlined = true;
    text[0].paragraph = true;

    if(exception != null) {
      text[1] = new TextData("The following error has occured:");
      text[1].color = "dark_red";
      text[1].paragraph = true;

      text[2] = new TextData(exception.getMessage() != null ? exception.getMessage() : exception.getClass()
                                                                                                .getSimpleName());
      text[2].color = "dark_red";
      text[2].paragraph = true;
    }

    text[3] = TextData.LINEBREAK;

    if(stackTrace != null){
      for(int i = 0; i < stackTrace.length && 5 + i * 2 < text.length; i++){
        text[4 + i * 2] = new TextData(stackTrace[i].toString());
        text[4 + i * 2].text += "\n";
        text[4 + i * 2].color = "dark_red";
        text[5 + i * 2] = TextData.LINEBREAK;
      }
    }

    list.add(new ElementText(0, TITLE_HEIGHT, GuiBook.PAGE_WIDTH, GuiBook.PAGE_HEIGHT - TITLE_HEIGHT, text));
  }
}
