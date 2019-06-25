package slimeknights.mantle.client.book.action.protocol;

import slimeknights.mantle.client.screen.book.BookScreen;

public abstract class ActionProtocol {

  public final String protocol;

  public ActionProtocol(String protocol) {
    this.protocol = protocol;
  }

  public abstract void processCommand(BookScreen book, String param);
}
