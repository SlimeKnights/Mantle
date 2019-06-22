package slimeknights.mantle.client.book.action.protocol;

import slimeknights.mantle.client.gui.book.GuiBook;

public class ProtocolGoToPage extends ActionProtocol {

  public static final String GO_TO = "go-to-page";
  public static final String GO_TO_RTN = GO_TO + "-rtn";

  private final boolean returner;

  public ProtocolGoToPage() {
    this(false, GO_TO);
  }

  public ProtocolGoToPage(boolean returner, String string) {
    super(string);

    this.returner = returner;
  }

  @Override
  public void processCommand(GuiBook book, String param) {
    int pageNum = book.book.findPageNumber(param);

    if(pageNum >= 0) {
      book.openPage(pageNum, this.returner);
    }
  }
}
