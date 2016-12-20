package slimeknights.mantle.client.gui.book.element;

public abstract class ElementButton extends SizedBookElement {

  protected final IButtonClickHandler callback;

  public final int buttonId;

  public ElementButton(int buttonId, int x, int y, int width, int height, IButtonClickHandler callback) {
    super(x, y, width, height);
    this.callback = callback;
    this.buttonId = buttonId;
  }

  @Override
  public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
    if(callback != null && isHovered(mouseX, mouseY)) {
      callback.onButtonClick(buttonId, this);
    }
  }
}
