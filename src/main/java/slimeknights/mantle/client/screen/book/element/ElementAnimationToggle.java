package slimeknights.mantle.client.screen.book.element;

import slimeknights.mantle.client.screen.book.ArrowButton;

public class ElementAnimationToggle extends ElementArrow {

  private final int arrowColorActive;
  private final int arrowColorInactive;
  private boolean toggled = false;

  public ElementAnimationToggle(int x, int y, ArrowButton.ArrowType arrowType, int arrowColor, int arrowColorHover, int arrowColorActive, ElementStructure elementStructure) {
    super(x, y, arrowType, arrowColor, arrowColorHover, (p_212998_1_) -> {
      elementStructure.canTick = !elementStructure.canTick;
    });

    this.arrowColorInactive = arrowColor;
    this.arrowColorActive = arrowColorActive;
  }

  @Override
  public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
    if (this.button != null && this.isHovered(mouseX, mouseY)) {
      this.button.onPress();
      this.toggled = !this.toggled;
      this.updateColor();
    }
  }

  protected void updateColor() {
    this.button.color = this.toggled ? this.arrowColorActive : this.arrowColorInactive;
  }
}
