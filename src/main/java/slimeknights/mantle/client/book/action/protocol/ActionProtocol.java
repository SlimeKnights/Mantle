package slimeknights.mantle.client.book.action.protocol;

import slimeknights.mantle.client.screen.book.BookScreen;

public abstract class ActionProtocol {
  public abstract void processCommand(BookScreen book, String param);
}
