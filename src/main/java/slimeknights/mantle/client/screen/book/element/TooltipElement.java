package slimeknights.mantle.client.screen.book.element;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.ITextComponent;

import java.util.List;

public class TooltipElement extends SizedBookElement {

  private final List<ITextComponent> tooltips;

  public TooltipElement(List<ITextComponent> tooltip, int x, int y, int width, int height) {
    super(x, y, width, height);

    this.tooltips = tooltip;
  }

  @Override
  public void draw(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks, FontRenderer fontRenderer) {
  }

  @Override
  public void drawOverlay(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks, FontRenderer fontRenderer) {
    if (this.isHovered(mouseX, mouseY)) {
      this.drawHoveringText(matrixStack, this.tooltips, mouseX, mouseY, fontRenderer);
    }
  }
}
