package slimeknights.mantle.client.screen.book.element;

import net.minecraft.client.gui.FontRenderer;

import java.util.List;

public class ElementTooltip extends SizedBookElement {
  private List<String> tooltip;

  public ElementTooltip(List<String> tooltip, int x, int y, int width, int height) {
    super(x, y, width, height);

    this.tooltip = tooltip;
  }

  @Override
  public void draw(int mouseX, int mouseY, float partialTicks, FontRenderer fontRenderer) {
  }

  @Override
  public void drawOverlay(int mouseX, int mouseY, float partialTicks, FontRenderer fontRenderer) {
    if (this.isHovered(mouseX, mouseY)) {
      this.drawHoveringText(this.tooltip, mouseX, mouseY, fontRenderer);
    }
  }
}
