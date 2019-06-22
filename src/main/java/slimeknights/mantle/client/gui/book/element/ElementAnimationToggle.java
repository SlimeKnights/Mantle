package slimeknights.mantle.client.gui.book.element;

import slimeknights.mantle.client.gui.book.GuiArrow;

public class ElementAnimationToggle extends ElementArrow {

  private final int arrowColorActive;
  private final int arrowColorInactive;
  private boolean toggled = false;

  public ElementAnimationToggle(int id, IButtonClickHandler handler, int x, int y, GuiArrow.ArrowType arrowType, int arrowColor, int arrowColorHover, int arrowColorActive) {
    super(id, handler, x, y, arrowType, arrowColor, arrowColorHover);

    this.arrowColorInactive = arrowColor;
    this.arrowColorActive = arrowColorActive;
  }

  @Override
  public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
    if(this.callback != null && this.isHovered(mouseX, mouseY)) {
      if(this.callback.onButtonClick(this.buttonId, this)) {
        this.toggled = !this.toggled;
        this.updateColor();
      }
    }
  }

  protected void updateColor() {
    this.button.color = this.toggled ? this.arrowColorActive : this.arrowColorInactive;
  }
}
