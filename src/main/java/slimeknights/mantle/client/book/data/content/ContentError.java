package slimeknights.mantle.client.book.data.content;

import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.data.BookLoadException;
import slimeknights.mantle.client.book.data.element.TextData;
import slimeknights.mantle.client.screen.book.BookScreen;
import slimeknights.mantle.client.screen.book.element.BookElement;
import slimeknights.mantle.client.screen.book.element.TextElement;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class ContentError extends PageContent {

  private final String errorStage;
  @Nullable
  private final Exception exception;

  public ContentError(String errorStage) {
    this(errorStage, null);
  }

  public ContentError(String errorStage, @Nullable Exception e) {
    this.errorStage = errorStage;
    this.exception = e;
  }

  @Override
  public void build(BookData book, ArrayList<BookElement> list, boolean rightSide) {
    this.addTitle(list, "Error");

    if (exception instanceof BookLoadException) {
      buildSimple(list);
      return;
    }

    StackTraceElement[] stackTrace = null;
    if (this.exception != null) {
      stackTrace = this.exception.getStackTrace();
    }

    TextData[] text = new TextData[1 + (this.exception != null ? 2 : 0) + (stackTrace != null ? 1 + Math.min(stackTrace.length * 2, 8) : 0)];
    text[0] = new TextData(this.errorStage);
    text[0].color = "dark_red";
    text[0].underlined = true;
    text[0].paragraph = true;

    if (this.exception != null) {
      text[1] = new TextData("The following error has occured:");
      text[1].color = "dark_red";
      text[1].paragraph = true;

      text[2] = new TextData(this.exception.getMessage() != null ? this.exception.getMessage() : this.exception.getClass().getSimpleName());
      text[2].color = "dark_red";
      text[2].paragraph = true;

      text[3] = TextData.LINEBREAK;
    }

    if (stackTrace != null) {
      for (int i = 0; i < stackTrace.length && 5 + i * 2 < text.length; i++) {
        text[4 + i * 2] = new TextData(stackTrace[i].toString());
        text[4 + i * 2].text += "\n";
        text[4 + i * 2].color = "dark_red";
        text[5 + i * 2] = TextData.LINEBREAK;
      }
    }

    list.add(new TextElement(0, TITLE_HEIGHT, BookScreen.PAGE_WIDTH, BookScreen.PAGE_HEIGHT - TITLE_HEIGHT, text));
  }

  public void buildSimple(ArrayList<BookElement> list) {
    TextData[] text = new TextData[1];

    text[0] = new TextData(exception.getMessage());
    text[0].color = "dark_red";

    list.add(new TextElement(0, TITLE_HEIGHT, BookScreen.PAGE_WIDTH, BookScreen.PAGE_HEIGHT - TITLE_HEIGHT, text));
  }
}
