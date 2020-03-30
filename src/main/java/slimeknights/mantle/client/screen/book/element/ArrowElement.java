package slimeknights.mantle.client.screen.book.element;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button.IPressable;
import slimeknights.mantle.client.screen.book.ArrowButton;

public class ArrowElement extends ButtonElement {

  protected final ArrowButton button;

  public ArrowElement(int x, int y, ArrowButton.ArrowType arrowType, int arrowColor, int arrowColorHover, IPressable iPressable) {
    super(x, y, arrowType.w, arrowType.h);

    this.button = new ArrowButton(x, y, arrowType, arrowColor, arrowColorHover, iPressable);
  }

  @Override
  public void draw(int mouseX, int mouseY, float partialTicks, FontRenderer fontRenderer) {
    this.button.render(mouseX, mouseY, partialTicks);
  }

  @Override
  public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
    if (this.button != null && this.isHovered(mouseX, mouseY)) {
      this.button.onPress();
    }
  }

}
