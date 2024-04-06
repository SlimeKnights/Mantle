package slimeknights.mantle.client.screen.book.element;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import slimeknights.mantle.client.book.action.StringActionProcessor;
import slimeknights.mantle.client.book.data.element.TextData;
import slimeknights.mantle.client.screen.book.TextDataRenderer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TextElement extends SizedBookElement {

  public TextData[] text;
  private final List<Component> tooltip = new ArrayList<Component>();

  private transient String lastAction = "";

  public TextElement(int x, int y, int width, int height, String text) {
    this(x, y, width, height, new TextData(text));
  }

  public TextElement(int x, int y, int width, int height, Collection<TextData> text) {
    this(x, y, width, height, text.toArray(new TextData[0]));
  }

  public TextElement(int x, int y, int width, int height, TextData... text) {
    super(x, y, width, height);

    this.text = text;
  }

  @Override
  public void draw(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks, Font fontRenderer) {
    lastAction = TextDataRenderer.drawText(graphics, this.x, this.y, this.width, this.height, this.text, mouseX, mouseY, fontRenderer, this.tooltip);
  }

  @Override
  public void drawOverlay(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks, Font fontRenderer) {
    if (this.tooltip.size() > 0) {
      drawTooltip(graphics, this.tooltip, mouseX, mouseY, fontRenderer);
      this.tooltip.clear();
    }
  }

  @Override
  public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
    if (mouseButton == 0 && !lastAction.isEmpty()) {
      StringActionProcessor.process(lastAction, this.parent);
    }
  }
}
