package slimeknights.mantle.client.screen.book.element;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Optional;

public class TooltipElement extends SizedBookElement {

  private final List<Component> tooltips;

  public TooltipElement(List<Component> tooltip, int x, int y, int width, int height) {
    super(x, y, width, height);

    this.tooltips = tooltip;
  }

  @Override
  public void draw(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks, Font fontRenderer) {
  }

  @Override
  public void drawOverlay(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks, Font fontRenderer) {
    if (this.isHovered(mouseX, mouseY)) {
      graphics.renderTooltip(fontRenderer, this.tooltips, Optional.empty(), mouseX, mouseY);
    }
  }
}
