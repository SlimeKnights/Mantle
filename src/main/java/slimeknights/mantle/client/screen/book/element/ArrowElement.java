package slimeknights.mantle.client.screen.book.element;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button.IPressable;
import slimeknights.mantle.client.screen.book.ArrowButton;

public class ArrowElement extends ButtonElement {

  protected final ArrowButton button;

  public ArrowElement(int x, int y, ArrowButton.ArrowType arrowType, int arrowColor, int arrowColorHover, IPressable iPressable) {
    super(x, y, arrowType.w, arrowType.h);
    // pass in book data during draw
    this.button = new ArrowButton(null, x, y, arrowType, arrowColor, arrowColorHover, iPressable);
  }

  @Override
  public void draw(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks, FontRenderer fontRenderer) {
    this.button.renderButton(matrixStack, mouseX, mouseY, partialTicks, parent.book);
  }

  @Override
  public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
    if (this.button != null && this.isHovered(mouseX, mouseY)) {
      this.button.onPress();
    }
  }

}
