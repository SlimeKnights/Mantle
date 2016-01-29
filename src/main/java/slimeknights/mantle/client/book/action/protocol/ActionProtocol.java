package slimeknights.mantle.client.book.action.protocol;

import slimeknights.mantle.client.gui.book.GuiBook;

public abstract class ActionProtocol {

  public final String protocol;

  public ActionProtocol(String protocol) {
    this.protocol = protocol;
  }

  public abstract void processCommand(GuiBook book, String param);
}
