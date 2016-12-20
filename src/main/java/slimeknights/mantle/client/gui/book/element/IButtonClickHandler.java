package slimeknights.mantle.client.gui.book.element;

public interface IButtonClickHandler {
  /** returns true if the button click was successful */
  boolean onButtonClick(int buttonId, ElementButton button);
}
