package slimeknights.mantle.client.book.action.protocol;

import slimeknights.mantle.client.gui.book.GuiBook;

public class ProtocolGoToPage extends ActionProtocol {

  private final boolean returner;

  public ProtocolGoToPage() {
    this(false, "");
  }

  public ProtocolGoToPage(boolean returner, String suffix) {
    super("go-to-page" + suffix);

    this.returner = returner;
  }

  @Override
  public void processCommand(GuiBook book, String param) {
    int pageNum = book.book.findPageNumber(param);

    if(pageNum >= 0)
      book.openPage(pageNum, returner);
  }
}
