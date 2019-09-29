package slimeknights.mantle.client.screen.book.element;

import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.fml.client.config.GuiUtils;
import slimeknights.mantle.client.screen.book.BookScreen;

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
      GuiUtils.drawHoveringText(tooltip, mouseX, mouseY, BookScreen.PAGE_WIDTH, BookScreen.PAGE_HEIGHT, BookScreen.PAGE_WIDTH, fontRenderer);
    }
  }
}
