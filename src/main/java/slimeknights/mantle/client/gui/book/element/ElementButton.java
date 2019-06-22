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
  public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
    if(this.callback != null && this.isHovered(mouseX, mouseY)) {
      this.callback.onButtonClick(this.buttonId, this);
    }
  }
}
