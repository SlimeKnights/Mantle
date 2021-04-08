package slimeknights.mantle.client.screen.book.element;

import java.util.List;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class TooltipElement extends SizedBookElement {

  private final List<Text> tooltips;

  public TooltipElement(List<Text> tooltip, int x, int y, int width, int height) {
    super(x, y, width, height);

    this.tooltips = tooltip;
  }

  @Override
  public void draw(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks, TextRenderer fontRenderer) {
  }

  @Override
  public void drawOverlay(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks, TextRenderer fontRenderer) {
    if (this.isHovered(mouseX, mouseY)) {
      this.drawHoveringText(matrixStack, this.tooltips, mouseX, mouseY, fontRenderer);
    }
  }
}
