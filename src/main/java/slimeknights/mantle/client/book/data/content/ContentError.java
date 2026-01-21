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

    // include exception information if present
    TextData[] text;
    if (exception != null) {
      StackTraceElement[] stackTrace = exception.getStackTrace();
      // include up to 4 lines in the stack trace
      text = new TextData[1 + 2 + (Math.min(stackTrace.length, 4))];

      // add exception
      text[1] = new TextData("The following error has occurred:");
      text[1].color = "dark_red";
      text[1].paragraph = true;

      String message = exception.getMessage();
      text[2] = new TextData(message != null && !message.isEmpty() ? message : exception.getClass().getSimpleName());
      text[2].color = "dark_red";
      text[2].paragraph = true;
      text[2].linebreak = true;

      // add stack trace
      for (int i = 3; i < text.length; i++) {
        text[i] = new TextData(stackTrace[i - 3].toString());
        text[i].text += "\n";
        text[i].color = "dark_red";
        text[i].linebreak = true;
      }
    } else {
      text = new TextData[1];
    }

    // add the error stage message at the top. this shows first but didn't want to type it twice
    text[0] = new TextData(this.errorStage);
    text[0].color = "dark_red";
    text[0].underlined = true;
    text[0].paragraph = true;

    list.add(new TextElement(0, TITLE_HEIGHT, BookScreen.PAGE_WIDTH, BookScreen.PAGE_HEIGHT - TITLE_HEIGHT, text));
  }

  public void buildSimple(ArrayList<BookElement> list) {
    TextData[] text = new TextData[1];

    text[0] = new TextData(exception != null ? exception.getMessage() : "");
    text[0].color = "dark_red";

    list.add(new TextElement(0, TITLE_HEIGHT, BookScreen.PAGE_WIDTH, BookScreen.PAGE_HEIGHT - TITLE_HEIGHT, text));
  }
}
