package slimeknights.mantle.client.book.data.content;

import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.data.BookLoadException;
import slimeknights.mantle.client.book.data.element.TextData;
import slimeknights.mantle.client.screen.book.BookScreen;
import slimeknights.mantle.client.screen.book.element.BookElement;
import slimeknights.mantle.client.screen.book.element.TextElement;

import java.util.ArrayList;

@OnlyIn(Dist.CLIENT)
public class ContentError extends PageContent {

  private final String errorStage;
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
    this.addTitle(list, new StringTextComponent("Error"));

    if (exception instanceof BookLoadException) {
      buildSimple(list);
      return;
    }

    StackTraceElement[] stackTrace = null;
    if (this.exception != null) {
      stackTrace = this.exception.getStackTrace();
    }

    TextData[] text = new TextData[1 + (this.exception != null ? 2 : 0) + (stackTrace != null ? 1 + Math.min(stackTrace.length * 2, 8) : 0)];
    text[0] = new TextData(new StringTextComponent(this.errorStage).mergeStyle(TextFormatting.DARK_RED, TextFormatting.UNDERLINE));
    text[0].isParagraph = true;

    if (this.exception != null) {
      text[1] = new TextData(new TranslationTextComponent("mantle.book.error_occurred").mergeStyle(TextFormatting.DARK_RED));
      text[1].isParagraph = true;

      String textMessage = this.exception.getMessage() != null ? this.exception.getMessage() : this.exception.getClass().getSimpleName();

      text[2] = new TextData(new StringTextComponent(textMessage).mergeStyle(TextFormatting.RED));
      text[2].isParagraph = true;
    }

    text[3] = TextData.LINEBREAK;

    if (stackTrace != null) {
      for (int i = 0; i < stackTrace.length && 5 + i * 2 < text.length; i++) {
        text[4 + i * 2] = new TextData(new StringTextComponent(stackTrace[i].toString() + "\n").mergeStyle(TextFormatting.DARK_RED));
        text[5 + i * 2] = TextData.LINEBREAK;
      }
    }

    list.add(new TextElement(0, TITLE_HEIGHT, BookScreen.PAGE_WIDTH, BookScreen.PAGE_HEIGHT - TITLE_HEIGHT, text));
  }

  public void buildSimple(ArrayList<BookElement> list) {
    TextData[] text = new TextData[1];

    text[0] = new TextData(new StringTextComponent(exception.getMessage()).mergeStyle(TextFormatting.DARK_RED));

    list.add(new TextElement(0, TITLE_HEIGHT, BookScreen.PAGE_WIDTH, BookScreen.PAGE_HEIGHT - TITLE_HEIGHT, text));
  }
}
