package slimeknights.mantle.client.screen.book.element;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import slimeknights.mantle.client.book.action.StringActionProcessor;
import slimeknights.mantle.client.book.data.element.TextComponentData;
import slimeknights.mantle.client.screen.book.TextComponentDataRenderer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TextComponentElement extends SizedBookElement {

  public TextComponentData[] text;
  private final List<Component> tooltip = new ArrayList<Component>();

  private transient String lastAction = "";

  public TextComponentElement(int x, int y, int width, int height, String text) {
    this(x, y, width, height, Component.literal(text));
  }

  public TextComponentElement(int x, int y, int width, int height, Component text) {
    this(x, y, width, height, new TextComponentData(text));
  }

  public TextComponentElement(int x, int y, int width, int height, Collection<TextComponentData> text) {
    this(x, y, width, height, text.toArray(new TextComponentData[0]));
  }

  public TextComponentElement(int x, int y, int width, int height, TextComponentData... text) {
    super(x, y, width, height);

    this.text = text;
  }

  @Override
  public void draw(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks, Font fontRenderer) {
    lastAction = TextComponentDataRenderer.drawText(matrixStack, this.x, this.y, this.width, this.height, this.text, mouseX, mouseY, fontRenderer, this.tooltip);
  }

  @Override
  public void drawOverlay(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks, Font fontRenderer) {
    if (this.tooltip.size() > 0) {
      drawTooltip(matrixStack, this.tooltip, mouseX, mouseY, fontRenderer);
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
