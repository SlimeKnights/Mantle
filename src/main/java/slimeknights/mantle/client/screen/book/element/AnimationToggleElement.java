package slimeknights.mantle.client.screen.book.element;

import slimeknights.mantle.client.screen.book.ArrowButton;

public class AnimationToggleElement extends ArrowElement {

  private final int arrowColorActive;
  private final int arrowColorInactive;
  private boolean toggled = false;

  public AnimationToggleElement(int x, int y, ArrowButton.ArrowType arrowType, int arrowColor, int arrowColorHover, int arrowColorActive, StructureElement structureElement) {
    super(x, y, arrowType, arrowColor, arrowColorHover, (p_212998_1_) -> {
      structureElement.canTick = !structureElement.canTick;
    });

    this.arrowColorInactive = arrowColor;
    this.arrowColorActive = arrowColorActive;
  }

  @Override
  public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
    if (this.button != null && this.isHovered(mouseX, mouseY)) {
      this.button.func_230930_b_();
      this.toggled = !this.toggled;
      this.updateColor();
    }
  }

  protected void updateColor() {
    this.button.color = this.toggled ? this.arrowColorActive : this.arrowColorInactive;
  }
}
